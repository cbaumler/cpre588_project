/****************************************************************************
*  Title: core.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Core Bitcoin protocol behavior
****************************************************************************/

#include <stdio.h>
#include "coreapi.h"

import "c_double_handshake";	// import the standard double handshake channel
import "c_mutex";	            // import the standard mutex channel
import "rpcserver";

behavior CoreInit (out Blockchain blockchain, out TransactionPool pool)
{
  void main (void)
  {
    blockchain.head_block = 0;
    pool.n_in_pool = 0;

    // TODO: Read these in from the stimulus. Make up genesis block for now.
    blockchain.entries[0].hash = 0x14a2483c;
    blockchain.entries[0].transactions[0].txid = 0;
    blockchain.entries[0].transactions[1].txid = 1;
  }
};

behavior Core (i_sender c_wallet_in, i_receiver c_wallet_out,
  i_sender c_swminer_in, i_receiver c_swminer_out,
  i_receiver c_transaction_in, i_sender c_transaction_out)
{

  // This data structure represents a local copy of the blockchain
  Blockchain blockchain;

  // This data structure represents the local pool of new transactions
  TransactionPool transaction_pool;

  // Current threshold below which a block header hash must be to be valid
  // TODO: Read this from the stimulus
  int target_threshold = 0xFFFFFF;

  // Channel for modifying the blockchain
  c_mutex block_mutex;

  // Channel for modifying the transaction pool
  c_mutex pool_mutex;

  // Behavior used to initialize the Bitcoin Core
  CoreInit init(blockchain, transaction_pool);

  // Create RPC server for communicating with mining software RPC client
  RPCServer mining_server(
    c_swminer_out,
    c_swminer_in,
    blockchain,
    blockchain,
    transaction_pool,
    transaction_pool,
    target_threshold,
    block_mutex,
    pool_mutex);

  // Create RPC server for communicating with wallet RPC client
    RPCServer wallet_server(
    c_wallet_out,
    c_wallet_in,
    blockchain,
    blockchain,
    transaction_pool,
    transaction_pool,
    target_threshold,
    block_mutex,
    pool_mutex);

  void main (void)
  {
    init.main();

    par {
      mining_server.main();
      wallet_server.main();
    }
  }

};
