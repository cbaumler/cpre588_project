/****************************************************************************
*  Title: rpcclient.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Remote Procedure Call (RPC) Client Behavior
****************************************************************************/

#include <stdio.h>
#include "coreapi.h"

import "c_double_handshake";	// import the standard channel

behavior RPCClient (i_sender c_request, i_receiver c_response)
{

  /*****************************************************************************
   * RPC call to get a block template for creating a new block
   *
   * Arguments:
   * p_template - a pointer to the block template
   *
   * Returns:
   *  1 success
   * -1 failure
   ****************************************************************************/
  int getblocktemplate (int id, BlockTemplate *p_template)
  {
    RPCMessage packet;
    int result;

    // Create a network packet with header and payload
    packet.type = GET_BLOCK_TEMPLATE;
    memcpy(&(packet.data.blocktemplate), p_template, sizeof(BlockTemplate));

    // Transmit the packet and wait for the response
    c_request.send(&packet, sizeof(packet));
    c_response.receive(&packet, sizeof(packet));

    // Return the packet data
    if (packet.type == GET_BLOCK_TEMPLATE)
    {
      memcpy(p_template, &(packet.data.blocktemplate), sizeof(BlockTemplate));
      result = 1;
    }
    else
    {
      result = -1;
    }
  }

/*****************************************************************************
 * RPC call to get the number of blocks in the local best blockchain
 *
 * Arguments: None
 *
 * Returns:
 * The block count on success
 * -1 on failure
 ****************************************************************************/
  int  getblockcount (void)
  {
    RPCMessage packet;
    int result;

    // Create a network packet with header
    packet.type = GET_BLOCK_COUNT;

    // Transmit the packet and wait for the response
    c_request.send(&packet, sizeof(packet));
    c_response.receive(&packet, sizeof(packet));

    // Return the packet data
    if (packet.type == GET_BLOCK_COUNT)
    {
      result = packet.block_count;
    }
    else
    {
      result = -1;
    }
    return result;
  }


/*****************************************************************************
 * RPC call to submit a block to the peer-to-peer network
 *
 * Arguments:
 * p_block - a pointer to the block to be submitted
 *
 * Returns: Void
 ****************************************************************************/
  void submitblock (Block *p_block)
  {
    RPCMessage packet;

    // Create a network packet with header and payload
    packet.type = SUBMIT_BLOCK;
    memcpy(&(packet.data.block), p_block, sizeof(Block));

    // Transmit the packet and wait for the response
    c_request.send(&packet, sizeof(packet));
    c_response.receive(&packet, sizeof(packet));

    if (packet.type != SUBMIT_BLOCK)
    {
      fprintf(stderr, "RPC Client: Incorrect server response, submitblock\n");
      exit (1);
    }
  }

  /*****************************************************************************
   * RPC call to get statistics about the confirmed unspent transaction output
   * (UTXO) set
   *
   * Arguments:
   * p_info - a pointer to the UTXO set info record
   *
   * Returns: Void
   *
   ****************************************************************************/
  void gettxoutsetinfo (TxOutSetInfo *p_info)
  {
    RPCMessage packet;

    // Create a network packet with header
    packet.type = GET_TX_OUT_SET_INFO;

    // Transmit the packet and wait for the response
    c_request.send(&packet, sizeof(packet));
    c_response.receive(&packet, sizeof(packet));

    if (packet.type == GET_TX_OUT_SET_INFO)
    {
      memcpy(p_info, &(packet.data.txoutsetinfo), sizeof(TxOutSetInfo));
    }
    else
    {
      fprintf(stderr, "RPC Client: Incorrect server response, txoutsetinfo\n");
      exit (1);
    }
  }

  /*****************************************************************************
   * RPC call to get details about a transaction output
   *
   * Arguments:
   * txid - the ID of the transaction containing the output
   * vout - the index of the output within the transaction
   * p_txout - a pointer to the transaction output detail record
   *
   * Returns: Void
   *
   ****************************************************************************/
  void gettxout (int txid, int vout, TxOut *p_txout)
  {
    RPCMessage packet;

    // Create a network packet with header and payload
    packet.type = GET_TX_OUT;
    packet.data.txout.txid = txid;
    packet.data.txout.vout = vout;

    // Transmit the packet and wait for the response
    c_request.send(&packet, sizeof(packet));
    c_response.receive(&packet, sizeof(packet));

    if (packet.type == GET_TX_OUT)
    {
      memcpy(p_txout, &(packet.data.txout), sizeof(TxOut));
    }
    else
    {
      fprintf(stderr, "RPC Client: Incorrect server response, gettxout\n");
      exit (1);
    }

  }


  /*****************************************************************************
   * RPC call to create a raw transaction
   *
   * Arguments:
   * input - the transaction input to spend (UTXO from another transaction)
   * address - the address of the payment recipient
   * amount - the amount being sent to the address
   *
   * Returns:
   * The raw transaction on success
   * -1 on failure
   *
   * Note: Due to simplification of the simulation design, createrawtransaction,
   * signrawtransaction, and sendrawtransaction must be called sequentially
   * for a particular transaction prior to creating any additional transactions.
   ****************************************************************************/
  int createrawtransaction (Outpoint input, int address, int amount)
  {
    RPCMessage packet;
    int result;

    // Create a network packet with header and payload
    packet.type = CREATE_RAW_TRANSACTION;
    memcpy(&(packet.data.transaction.input), &input, sizeof(input));
    packet.data.transaction.address = address;
    packet.data.transaction.amount = amount;

    // Transmit the packet and wait for the response
    c_request.send(&packet, sizeof(packet));
    c_response.receive(&packet, sizeof(packet));

    if (packet.type == CREATE_RAW_TRANSACTION)
    {
      result = packet.transaction.raw_transaction;
    }
    else
    {
      result = -1;
    }
    return result;
  }

  /*****************************************************************************
   * RPC call to sign a raw transaction using a private key
   *
   * Arguments:
   * raw_transaction - the raw transaction to sign
   * prviate_key - the key used to sign the transaction
   *
   * Returns:
   * The signed transaction on success
   * -1 on failure
   ****************************************************************************/
  int signrawtransaction (int raw_transaction, int private_key)
  {
    RPCMessage packet;
    int result;

    // Create a network packet with header and payload
    packet.type = SIGN_RAW_TRANSACTION;
    packet.data.transaction.raw_transaction = raw_transaction;
    packet.data.transaction.private_key = private_key;

    // Transmit the packet and wait for the response
    c_request.send(&packet, sizeof(packet));
    c_response.receive(&packet, sizeof(packet));

    if (packet.type == SIGN_RAW_TRANSACTION)
    {
      result = packet.transaction.signed_transaction;
    }
    else
    {
      result = -1;
    }
    return result;
  }

  /*****************************************************************************
   * RPC call to send a signed raw transaction to the peer-to-peer network
   *
   * Arguments:
   * signed_transaction - the signed raw transaction to broadcast
   *
   * Returns:
   * The transaction ID on success
   * -1 on failure
   ****************************************************************************/
  int sendrawtransaction (int signed_transaction)
  {
    RPCMessage packet;
    int result;

    // Create a network packet with header and payload
    packet.type = SEND_RAW_TRANSACTION;
    packet.data.transaction.signed_transaction = signed_transaction;

    // Transmit the packet and wait for the response
    c_request.send(&packet, sizeof(packet));
    c_response.receive(&packet, sizeof(packet));

    if (packet.type == SEND_RAW_TRANSACTION)
    {
      result = packet.transaction.txid;
    }
    else
    {
      result = -1;
    }
    return result;
  }

  void main (void)
  {

  }
};
