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

  // An event to notify the RPC servers when to start
  event start_servers;

  // Create RPC server for communicating with mining software RPC client
  RPCServer mining_server(
    c_swminer_out,
    c_swminer_in,
    blockchain,
    blockchain,
    transaction_pool,
    transaction_pool,
    target_threshold,
    start_servers,
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
    start_servers,
    block_mutex,
    pool_mutex);

  // Create a manager for the blockchain
  //BlockchainManager blockchain_manager(&blockchain, &transaction_pool,
  //  block_mutex, pool_mutex, start_servers);

  void main (void)
  {
    blockchain.head_block = 0;
    transaction_pool.n_in_pool = 0;

    par {
      mining_server.main();
      wallet_server.main();
      //blockchain_manager.main();
    }
  }

};
