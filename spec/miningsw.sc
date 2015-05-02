/****************************************************************************
*  Title: miningsw.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Bitcoin mining software behavior
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include "../api/coreapi.h"

#define MINER_ID   0

import "c_double_handshake";	// import the standard channel
import "rpcclient";

behavior MiningSW (
  i_sender c_request,
  i_receiver c_response,
  i_sender c_blk_hdr,
  i_receiver c_nonce)
{
  RPCClient client(c_request, c_response);

  void main(void)
  {
    int err, idx, block_count;
    BlockTemplate btemplate;
    Block block;

    // TODO: This is test code to show how to use the RPC interface.
    // It should be replaced with the actual mining software code.

    // Get a block template for creating a block header
    err = client.getblocktemplate(MINER_ID, &btemplate);
    if (err == -1)
    {
      fprintf(stderr, "miningsw: getblocktemplate failed\n");
      exit (1);
    }

    printf("--------------\nMining Software Debug:\n");
    printf("version=%d\n", btemplate.version);
    printf("previous_block_hash=%d\n", btemplate.previous_block_hash);
    printf("num_transactions=%d\n", btemplate.num_transactions);
    for (idx = 0; idx < btemplate.num_transactions; idx++)
    {
      printf("txid=%d\n", btemplate.transactions[idx].txid);
    }
    printf("current_time=%d\n", btemplate.current_time);
    printf("bits=%d\n", btemplate.bits);

    // Get the the number of blocks in the blockchain
    block_count = client.getblockcount();
    if (block_count == -1)
    {
      fprintf(stderr, "miningsw: getblockcount failed\n");
      exit (1);
    }

    printf("block_count=%d\n", block_count);

    // Submit Block
    block.hash = 0x12345;
    block.n_transactions = btemplate.num_transactions;
    memcpy(block.transactions, btemplate.transactions, sizeof (block.transactions));
    err = client.submitblock(&block);
    if (err == -1)
    {
      fprintf(stderr, "miningsw: submitblock failed\n");
      exit (1);
    }

  }

};
