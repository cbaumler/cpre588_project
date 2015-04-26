/****************************************************************************
*  Title: core.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Core Bitcoin protocol behavior
****************************************************************************/

#include <stdio.h>
#include <time.h>
#include "coreapi.h"

#define BLOCK_VERSION      2
#define BLOCKCHAIN_MAX    64

import "c_double_handshake";	// import the standard channel

behavior Core (i_sender c_wallet_in, i_receiver c_wallet_out,
  i_sender c_swminer_in, i_receiver c_swminer_out,
  i_receiver c_transaction_in, i_sender c_transaction_out)
{

  // This data structure represents a local copy of the blockchain
  Block blockchain[BLOCKCHAIN_MAX];
  int head_block = 0;

  // This data structure represents the local pool of new transactions
  Transaction transaction_pool[MAX_TRANSACTIONS];
  int n_transactions_in_pool = 0;

  // Current threshold below which a block header hash must be to be valid
  // TODO: Read this from the stimulus
  int target_threshold = 0xFFFFFF;

  // This function initializes the blockchain
  void build_blockchain (void)
  {
    // TODO: Read these in from the stimulus. Make up genesis block for now.
    blockchain[head_block].hash = 0x14a2483c;
    blockchain[head_block].transactions[0].txid = 0;
    blockchain[head_block].transactions[1].txid = 1;
  }

  // This function appends a block to the blockchain
  void append_to_blockchain (Block block)
  {
    head_block++;
    memcpy(&(blockchain[head_block]), &block, sizeof(Block));
  }

  // This function adds a transaction to the local pool
  void add_transaction_to_pool (Transaction transaction)
  {
    if ((n_transactions_in_pool + 1) < MAX_TRANSACTIONS)
    {
      transaction_pool[n_transactions_in_pool].txid = transaction.txid;
      n_transactions_in_pool++;
    }
  }

  void rpc_send_block_template (int id)
  {
    int idx;
    RPCMessage message;
    GetBlockTemplate *param;

    param = &(message.data.getblocktemplate);

    // Fill in the message data
    message.type = GET_BLOCK_TEMPLATE_RESP;
    param->id = id;
    param->version = BLOCK_VERSION;
    param->previous_block_hash = blockchain[head_block].hash;
    for (idx = 0; idx < n_transactions_in_pool; idx++)
    {
      param->transactions[idx].txid = transaction_pool[idx].txid;
    }
    n_transactions_in_pool = 0;
    param->current_time = (int)time(0);
    param->bits = target_threshold;

    c_swminer_in.send(&message, sizeof(message));
  }

  void rpc_send_block_count (void)
  {
    RPCMessage message;

    message.type = GET_BLOCK_COUNT_RESP;
    message.data.getblockcount.block_count = head_block+1;

    c_swminer_in.send(&message, sizeof(message));
  }

  void rpc_receive (void)
  {
    RPCMessage message;

    c_swminer_out.receive(&message, sizeof(message));
    switch (message.type)
    {
      case GET_BLOCK_TEMPLATE_REQ:
      {
        // Send a block template
        rpc_send_block_template(message.data.getblocktemplate.id);
        break;
      }
      case GET_BLOCK_COUNT_REQ:
      {
        // Send the current block count
        rpc_send_block_count();
        break;
      }
      case SUBMIT_BLOCK:
      {
        // TODO validate block?
        // TODO transmit block to the network?
        // Add the new block to the blockchain
        append_to_blockchain(message.data.submitblock.block);
      }
      default:
      {
        fprintf(stderr, "Core received invalid RPC message\n");
        break;
      }
    }
  }

  void main (void)
  {
    // TODO: Split this behavior up into several behaviors
    build_blockchain();
    rpc_receive();
  }

};
