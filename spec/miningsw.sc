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

      for (idx = 0; idx < NUM_HASH_BYTES; idx++)
      {
        // TODO: Compute an actual hash if time permits
        block.header.merkle_root[idx] = (unsigned char)(rand()%255);
      }

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
