/****************************************************************************
*  Title: coreapi.h
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Defines the Bitcoin APIs. Note that some APIs have been
*               simplified per the scope of this project.
****************************************************************************/

#define MAX_TRANSACTIONS   10

// These are the types of RPC messages
typedef enum
{
  // Mining API
  GET_BLOCK_TEMPLATE_REQ,
  GET_BLOCK_TEMPLATE_RESP,
  GET_BLOCK_COUNT_REQ,
  GET_BLOCK_COUNT_RESP,
  SUBMIT_BLOCK,

  // Wallet API
  GET_TX_OUT,
  GET_TX_OUT_SET_INFO,
  CREATE_RAW_TRANSACTION,
  SEND_RAW_TRANSACTION,
  SIGN_RAW_TRANSACTION

} RPCType;

// Simplified Bitcoin transaction type
typedef struct
{
  int txid;

} Transaction;

// Simplified Bitcoin block type
typedef struct
{
  int hash;
  Transaction transactions[MAX_TRANSACTIONS];

} Block;

// Requests a block template for use with mining software
// When submitting a request, only "id" needs to be assigned a value
typedef struct
{
  int id;
  int version;
  int previous_block_hash;
  int num_transactions;
  Transaction transactions[MAX_TRANSACTIONS];
  int current_time;
  int bits;

} GetBlockTemplate;

// Returns the number of blocks in the local best block chain
typedef struct
{
  int block_count;

} GetBlockCount;

// Accepts a block, verifies it, and broadcasts it to the network
typedef struct
{
  Block block;

} SubmitBlock;

typedef union
{
  GetBlockTemplate getblocktemplate;
  GetBlockCount getblockcount;
  SubmitBlock submitblock;

} RPCData;

// This is the top level RPC Message structure
typedef struct
{
  RPCType type;
  RPCData data;

} RPCMessage;
