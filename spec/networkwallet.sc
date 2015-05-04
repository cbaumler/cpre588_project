/****************************************************************************
*  Title: networkwallet.sc
*  Author: Team 4
*  Date:
*  Description: The network wallet works in conjunction with the
*  signing-only wallet
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include "../api/coreapi.h"

import "c_double_handshake";	// import the standard channel
import "rpcclient";

#define USER_ADDRESS   2222222

behavior NetworkWallet (i_sender c_request, i_receiver c_response,
  i_sender c_hw_wallet_in, i_receiver c_hw_wallet_out, i_receiver c_spend,
  in event e_log_wallet, i_sender c_wallet_log)
{
  RPCClient client(c_request, c_response);
  int tx_payments_received[MAX_UTXO];
  int num_payments_received = 0;
  int tx_payments_sent[MAX_UTXO];
  int num_payments_sent = 0;
  int user_utxos[MAX_UTXO];
  int num_user_utxos;

  int get_user_balance(void)
  {
    TxOutSetInfo setinfo;
    TxOut utxoinfo;
    int err, idx;
    int user_balance = 0;

    num_payments_received = 0;
    num_user_utxos = 0;

    // Get the set of confirmed unspent transaction outputs (UTXOs)
    err = client.gettxoutsetinfo(&setinfo);
    if (err == -1)
    {
      fprintf(stderr, "wallet: gettxoutsetinfo failed\n");
      exit (1);
    }

    // Loop through each UTXO
    for (idx = 0; idx < setinfo.txouts; idx++)
    {
      // Look up details for this UTXO
      err = client.gettxout(setinfo.utxo[idx].txid, setinfo.utxo[idx].vout, &utxoinfo);
      if (err == -1)
      {
        fprintf(stderr, "wallet: gettxout failed\n");
        exit (1);
      }

      // Check if the UTXO belongs to the user
      if (utxoinfo.address == USER_ADDRESS)
      {
        // Add the UTXO's amount to the user's balance
        user_balance += utxoinfo.value;

        // Keep track of the user's UTXOs
        user_utxos[num_user_utxos] = setinfo.utxo[idx].txid;
        num_user_utxos++;

        // Track the payment
        tx_payments_received[num_payments_received] = utxoinfo.txin_id;
        num_payments_received++;
      }
    }

    return user_balance;
  }

  void spend_bitcoin(void)
  {
    Outpoint input_tx;
    bool spend;
    bool request_key = true;
    int err, key, txid;
    TxOut info;
    int raw_transaction;
    int signed_transaction;
    int balance;

    // Wait for a command to spend Bitcoin
    c_spend.receive(&spend, sizeof(spend));

    // Refresh the user's balance data
    balance = get_user_balance();
    printf("Wallet: Balance prior to transaction is %d BTC\n", balance);

    // Check if the user has UTXOs available
    if (num_user_utxos > 0)
    {
      // Spend the Bitcoin from the first available UTXO
      input_tx.txid = user_utxos[0];
      input_tx.vout = 0; // Always 0 in our simplified implementation

      // Spend the value stored in the UTXO
      err = client.gettxout(input_tx.txid, input_tx.vout, &info);
      if (err == -1)
      {
        fprintf(stderr, "wallet: gettxout failed\n");
        exit (1);
      }

      raw_transaction = client.createrawtransaction(input_tx, rand(), info.value);
      if (raw_transaction == -1)
      {
        fprintf(stderr, "wallet: createrawtransaction failed\n");
        exit (1);
      }

      // Get a key from the hardware wallet
      c_hw_wallet_in.send(&request_key, sizeof(request_key));
      c_hw_wallet_out.receive(&key, sizeof(key));

      // Sign the transaction
      signed_transaction = client.signrawtransaction(raw_transaction, key);
      if (signed_transaction == -1)
      {
        fprintf(stderr, "wallet: signrawtransaction failed\n");
        exit (1);
      }

      // Send the transaction to the P2P network
      txid = client.sendrawtransaction(signed_transaction);
      if (txid == -1)
      {
        fprintf(stderr, "wallet: sendrawtransaction failed\n");
        exit (1);
      }
      tx_payments_sent[num_payments_sent] = txid;
      num_payments_sent++;
      printf("Wallet: Sent %d BTC Transaction to P2P Network\n", info.value);
    }
    else
    {
      // No Bitcoin available in wallet
      printf("No Bitcoin available to spend\n");
    }
  }

  void send_log(void)
  {
    int log_data;

    c_wallet_log.send(&log_data, sizeof(log_data));
  }

  void main (void)
  {
    while (true)
    {
      while (true)
      {
        //try {spend_bitcoin();}
        //trap (e_log_wallet) {this.send_log();}

        spend_bitcoin();

      }
    }
  }
};
