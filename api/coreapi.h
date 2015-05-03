/****************************************************************************
*  Title: coreapi.h
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Defines the Bitcoin APIs. Note that some APIs have been
*               simplified per the scope of this project.
****************************************************************************/

#define BLOCK_VERSION                    2
#define MAX_TRANSACTIONS                10
#define MAX_BLOCKCHAIN_LENGTH           64
#define MAX_UTXO                      1024
#define NUM_HASH_BYTES                  32

#define NUM_CODE_BLOCKS                 26
#define MAX_MINED_BLOCKS                48


// These are the types of RPC messages
typedef enum
{
  // Mining API
  GET_BLOCK_TEMPLATE,
  GET_BLOCK_COUNT,
  SUBMIT_BLOCK,

  // Wallet API
  GET_TX_OUT,
  GET_TX_OUT_SET_INFO,
  CREATE_RAW_TRANSACTION,
  SIGN_RAW_TRANSACTION,
  SEND_RAW_TRANSACTION,

  // Development API
  DEV_SEND_TRANSACTION

} RPCType;

// A data structure used to refer to a particular transaction output
typedef struct
{
  int txid; // ID of transaction where this output appears as unspent (UTXO)
  int vout; // Output index number

} Outpoint;

// Representative Bitcoin transaction type
// Transactions have been simplified to only allow one input/output and
// one address/amount per transaction to reduce project scope
typedef struct
{
  int txid;               // The transaction identifier
  Outpoint input;         // The transaction input
  Outpoint output;        // The transaction output
  int address;            // The address to which Bitcoins are sent
  int amount;             // Amount being sent to the address
  int private_key;        // Private key used to sign transaction.
  int raw_transaction;    // Representative type for the raw transaction
  int signed_transaction; // Representative type for the signed transaction

} Transaction;

// A data structure containing statistics about the confirmed unspent
// transaction output (UTXO) set
typedef struct
{
  // The height of the local best block chain A new node with only the
  // hardcoded genesis block will have a height of 0
  int height;

  // The hash of the header of the highest block on the local best blockchain
  unsigned char best_block[NUM_HASH_BYTES];

  int transactions;  // The number of transactions with unspent outputs
  int txouts;        // The number of unspent transaction outputs
  int total_amount;  // The total number of Bitcoins in the UTXO set

  Outpoint utxo[MAX_UTXO]; // The UTXO set

} TxOutSetInfo;

// A data structure containing details about a transaction output
typedef struct
{
  // The hash of the header of the block on the local best
  // blockchain which includes this transaction
  unsigned char best_block[NUM_HASH_BYTES];

  int value;         // The amount spent to this output. May be 0.
  int address;       // The address to which Bitcoins were sent
  int txid;          // The transaction ID containing the output
  int vout;          // The index of the output within the transaction

} TxOut;

// Representative Bitcoin block header type
typedef struct
{
  unsigned int  version;                       // Bitcoin protocol version
  unsigned char prev_hash[NUM_HASH_BYTES];     // Hash of previous block header
  unsigned int  current_time;                  // Timestamp
  unsigned char merkle_root[NUM_HASH_BYTES];   // Constructed from txids
  unsigned int  nbits;                         // Difficulty threshold
  unsigned int  nonce;                         // Random number

} BlockHeader;

// Representative Bitcoin block type
typedef struct
{
  BlockHeader header;                           // The block header
  unsigned char hash[NUM_HASH_BYTES];           // Hash of block header
  Transaction transactions[MAX_TRANSACTIONS];   // Transactions in block
  int n_transactions;                           // Number of transactions

} Block;

typedef struct
{
  Block entries[MAX_BLOCKCHAIN_LENGTH];
  int head_block;

} Blockchain;

typedef struct
{
  Transaction pool[MAX_TRANSACTIONS];
  int n_in_pool;

} TransactionPool;

// A block template for use with mining software
typedef struct
{
  unsigned int version;                        // Bitcoin protocol version
  unsigned char prev_hash[NUM_HASH_BYTES];     // Hash of previous block header
  unsigned int num_transactions;               // Number of transactions available
  Transaction transactions[MAX_TRANSACTIONS];  // Array of transactions
  unsigned int current_time;                   // Timestamp
  unsigned int bits;                           // Difficulty threshold

} BlockTemplate;

typedef union
{
  BlockTemplate blocktemplate;
  int block_count; // Number of blocks in blockchain. 0 if genesis block only.
  Block block;
  Transaction transaction;
  TxOutSetInfo txoutsetinfo;
  TxOut txout;

} RPCData;

// This is the top level RPC Message structure
typedef struct
{
  RPCType type;
  RPCData data;

} RPCMessage;

typedef struct
{
  int time;
  int power;

} Stats;

typedef struct {

  // Time statistics
  long long time_total;     // Time since start of simulation
  long long time_blk_start; // Time at start of block processing
  long long time_blk_stop;  // Time at end of current block processing
  long long time_blk;       // Time for processing current block

  // Hash count statistics
  long hash_total;          // Total hash count since start of simulation
  long hash_blk;            // Current hash count accumulator for current block

  // Power statistics
  long power_total;         // Total power since start of simulation
  long power_blk;           // Current power accumulator for current block

  Stats codeblocks[NUM_CODE_BLOCKS];
  Stats mined_blocks[MAX_MINED_BLOCKS];
  int num_mined_blocks;
  int total_power;
  int total_num_hashes;
  int total_sim_time;
  int total_cost;
  
} PerformanceData;

