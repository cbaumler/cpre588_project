/****************************************************************************
*  Title: core.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Core Bitcoin protocol behavior
****************************************************************************/

#include <stdio.h>
#include <time.h>
#include "../api/coreapi.h"

import "c_double_handshake";	// import the standard double handshake channel
import "c_mutex";	            // import the standard mutex channel
import "rpcserver";

behavior CoreInit (out Blockchain blockchain, out TransactionPool transaction_pool)
{
  void main (void)
  {
    int idx;

    blockchain.head_block = 0;
    transaction_pool.n_in_pool = 0;

    // Create a genesis block to initialize the blockchain
    for (idx = 0; idx < NUM_HASH_BYTES; idx++)
    {
      blockchain.entries[0].hash[idx] = (unsigned char)(rand()%255);
    }
    //blockchain.entries[0].transactions[0].txid = 0;
    //blockchain.entries[0].transactions[1].txid = 1;
    blockchain.entries[0].n_transactions = 0;

    // TODO: Make up some transactions in the pool for now
    //transaction_pool.pool[0].txid = 2;
    //transaction_pool.pool[1].txid = 3;
    //transaction_pool.n_in_pool = 2;
  }
};

behavior Core (i_receiver c_wallet_request, i_sender c_wallet_response,
  i_receiver c_swminer_request, i_sender c_swminer_response,
  i_receiver c_p2p_request, i_sender c_p2p_response,
  in unsigned int mining_difficulty)
{

  // This data structure represents a local copy of the blockchain
  Blockchain blockchain;

  // This data structure represents the local pool of new transactions
  TransactionPool transaction_pool;

  // Channel for modifying the blockchain
  c_mutex block_mutex;

  // Channel for modifying the transaction pool
  c_mutex pool_mutex;

  // Behavior used to initialize the Bitcoin Core
  CoreInit init(blockchain, transaction_pool);

  // Create RPC server for communicating with mining software RPC client
  RPCServer mining_server(
    c_swminer_request,
    c_swminer_response,
    blockchain,
    blockchain,
    transaction_pool,
    transaction_pool,
    mining_difficulty,
    block_mutex,
    pool_mutex);

  // Create RPC server for communicating with wallet RPC client
    RPCServer wallet_server(
    c_wallet_request,
    c_wallet_response,
    blockchain,
    blockchain,
    transaction_pool,
    transaction_pool,
    mining_difficulty,
    block_mutex,
    pool_mutex);

  // Create RPC server for communicating with Bitcoin Peer-to-Peer network
    RPCServer p2p_server(
    c_p2p_request,
    c_p2p_response,
    blockchain,
    blockchain,
    transaction_pool,
    transaction_pool,
    mining_difficulty,
    block_mutex,
    pool_mutex);

  void main (void)
  {
    init.main();

    par {
      mining_server.main();
      wallet_server.main();
      p2p_server.main();
    }
  }

};
