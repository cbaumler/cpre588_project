/****************************************************************************
*  Title: miningsw.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Bitcoin mining software behavior
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include "../api/coreapi.h"

#define MINER_ID   0

import "c_double_handshake";	// import the standard channel
import "rpcclient";

typedef struct {
  unsigned int nonce;
	bool status;
} Return_Nonce;

behavior MinerThread (i_receiver c_header, i_receiver c_nonce,
  i_sender c_blk_hdr, out unsigned int nonce)
{

  void main (void)
  {
    BlockHeader header;
    Return_Nonce hw_return;

    while (true)
    {
      // Get a block header
      c_header.receive(&header, sizeof(header));

      // Send the block header to the hardware miner
      printf("sw_miner: sending block header\n");
      c_blk_hdr.send(&header, sizeof(header));

      // Wait for the hardware miner to return a status
      c_nonce.receive(&hw_return, sizeof(hw_return));
      if (hw_return.status)
      {
        // Got a valid nonce from the hardware miner. Send it to the controller
        nonce = hw_return.nonce;
      }
      else
      {
        // HW Miner didn't compute a nonce. Send another block header
        // and start over.
      }

    }
  }
};

behavior Controller(i_sender c_request, i_receiver c_response,
  i_sender c_header, i_sender c_abort, in unsigned int nonce)
{

  RPCClient client(c_request, c_response);
  Block block;

  int hash_txids(int in1, int in2)
  {
    // Instead of actually computing a hash, just add the ids together.
    // This simplification reduces scope and doesn't affect the simulation.
    return (in1 + in2);
  }

  int compute_merkle_root (BlockTemplate btemplate)
  {
    int txids[MAX_TRANSACTIONS];
    int num_tx_left, num_tx_pairs, idx, idx1, idx2;
    int merkle_root;

    // Compute the merkle root by hashing together txids as follows:
    //
    //      ABCDEEEE .......Merkle root
    //        /        \
    //     ABCD        EEEE
    //    /    \      /
    //   AB    CD    EE .......E is paired with itself
    //  /  \  /  \  /
    //  A  B  C  D  E .........Transactions

    // Get the transaction IDs (txids) of all the transactions in the block
    for (idx = 0; idx < btemplate.num_transactions; idx++)
    {
      txids[idx] = btemplate.transactions[idx].txid;
    }

    // If the number of transactions is odd, duplicate the last txid
    // We need an even number of txids to compute the merkle root
    if (btemplate.num_transactions % 2)
    {
      txids[idx] = txids[idx-1];
      num_tx_pairs = (btemplate.num_transactions + 1) / 2;
    }
    else
    {
      num_tx_pairs = btemplate.num_transactions / 2;
    }

    // Begin hashing togther pairs of txids
    while (num_tx_pairs > 0)
    {
      idx2 = 0;
      num_tx_left = 0;
      for (idx1 = 0; idx1 < num_tx_pairs; idx1++)
      {
         txids[idx1] = hash_txids(txids[idx2], txids[idx2+1]);
         idx2 += 2;
         num_tx_left++;
      }

      if (num_tx_left == 1)
      {
        // We've reached the root
        merkle_root = txids[0];
        num_tx_pairs = 0;
      }
      else if (num_tx_left % 2)
      {
        // Odd number left, duplicate the last one
        num_tx_pairs = (num_tx_left + 1) / 2;
        txids[idx1] = txids[idx1-1];
      }
      else
      {
        // Even number left
        num_tx_pairs = num_tx_left / 2;
      }
    }
  }

  void send_header()
  {
    int err;
    BlockTemplate btemplate;

    // Get a block template for creating a block header
    err = client.getblocktemplate(MINER_ID, &btemplate);
    if (err == -1)
    {
      fprintf(stderr, "miningsw: getblocktemplate failed\n");
      exit (1);
    }

    // Create a block header
    block.header.version = btemplate.version;
    memcpy(block.header.prev_hash, btemplate.prev_hash, NUM_HASH_BYTES);
    block.header.current_time = (unsigned int)time(0);
    block.header.nbits = btemplate.bits;
    printf("!!!mining difficulty = %d\n", block.header.nbits);
    block.header.merkle_root = compute_merkle_root(btemplate);

    block.n_transactions = btemplate.num_transactions;
    memcpy(block.transactions, btemplate.transactions, sizeof (block.transactions));

    // Send the block header to the miner thread
    c_header.send(&(block.header), sizeof(block.header));

  }

  int update_block_count()
  {
    int block_count;

    // Get the number of blocks in the blockchain
    block_count = client.getblockcount();
    if (block_count == -1)
    {
      fprintf(stderr, "miningsw: getblockcount failed\n");
      exit (1);
    }

    return block_count;
  }

  void main (void)
  {
    unsigned int old_nonce = 0;
    int err, idx, block_count, old_block_count;
    int abort_command = 2;

    // Get the current number of blocks in the blockchain
    block_count = update_block_count();

    // Send a block header to the miner thread
    send_header();

    while (true)
    {
      old_block_count = block_count;
      block_count = update_block_count();

      // Check if a new block was added to the blockchain by the P2P network
      if (block_count > old_block_count)
      {
        // Tell the HW Miner to abort the current attempt
        c_abort.send(&abort_command, sizeof(abort_command));

        // Send a new block header to the miner thread
        send_header();
      }
      // Check if we received a valid nonce from the HW miner
      else if (old_nonce != nonce)
      {
        // The HW Miner returned a valid nonce
        old_nonce = nonce;

        // Submit a block to the P2P network
        block.header.nonce = nonce;
        for (idx = 0; idx < NUM_HASH_BYTES; idx++)
        {
          block.hash[idx] = (unsigned char)(rand()%255);
        }
        err = client.submitblock(&block);
        if (err == -1)
        {
          fprintf(stderr, "miningsw: submitblock failed\n");
          exit (1);
        }
        else
        {
          block_count++;
        }
      }
      else
      {
        // Sleep for a while
        waitfor(50);
      }
    }
  }

};

behavior MiningSW (
  i_sender c_request,
  i_receiver c_response,
  i_sender c_blk_hdr,
  i_receiver c_nonce,
  i_sender c_abort)
{

  c_double_handshake c_header;
  unsigned int nonce = 0;

  MinerThread miner_thread(c_header, c_nonce, c_blk_hdr, nonce);
  Controller controller(c_request, c_response, c_header, c_abort, nonce);

  void main(void)
  {
    par {
      miner_thread.main();
      controller.main();
    }
  }

};
