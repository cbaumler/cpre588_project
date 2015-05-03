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

behavior MiningSW (
  i_sender c_request,
  i_receiver c_response,
  i_sender c_blk_hdr,
  i_receiver c_nonce)
{

  RPCClient client(c_request, c_response);

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

  void main(void)
  {
    int err, block_count, idx;
    BlockTemplate btemplate;
    Block block;
    Return_Nonce nonce;

    while (1)
    {

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
      block.header.merkle_root = compute_merkle_root(btemplate);

      // Send the block header to the hardware miner
    	printf("sw_miner: sending block header\n");
      c_blk_hdr.send(&block.header, sizeof(block.header));

      // Wait for the hardware miner to find a nonce that gives a valid hash
      c_nonce.receive(&nonce, sizeof(nonce));
      if (nonce.status)
      {
        block.header.nonce = nonce.nonce;
      }
      else
      {
        // TODO: Handle failure to find nonce
        printf("swminer: failed to find nonce\n");
      }
  	  printf("sw_miner: rx nonce: %u, valid: %d\n", nonce.nonce, nonce.status);

      // TODO: We need to be able to interrupt the hw miner if a block is added
      // to the blockchain by the P2P network

      // DJK: c_abort.send();

      // Get the the number of blocks in the blockchain
/*
      block_count = client.getblockcount();
      if (block_count == -1)
      {
        fprintf(stderr, "miningsw: getblockcount failed\n");
        exit (1);
      }
*/

      // Submit the block to the P2P network
      // TODO
/*
      //block.hash = 0x12345;
      block.n_transactions = btemplate.num_transactions;
      memcpy(block.transactions, btemplate.transactions, sizeof (block.transactions));
      err = client.submitblock(&block);
      if (err == -1)
      {
        fprintf(stderr, "miningsw: submitblock failed\n");
        exit (1);
      }
*/
    }
  }

};
