/****************************************************************************
*  Title: wallet.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Bitcoin wallet top-level behavior
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include "coreapi.h"

import "c_double_handshake";	// import the standard channel
import "rpcclient";

behavior Wallet(i_sender c_request, i_receiver c_response)
{
  RPCClient client(c_request, c_response);

  void main(void)
  {
    int err, idx, txid;
    TxOutSetInfo info;
    TxOut txInfo;
    Outpoint input_tx;
    int raw_transaction, signed_transaction;

    printf("--------------\nWallet Debug:\n");

    err = client.gettxoutsetinfo(&info);
    if (err == -1)
    {
      fprintf(stderr, "wallet: gettxoutsetinfo failed\n");
      exit (1);
    }
    printf("height=%d\n", info.height);
    printf("best_block=%d\n", info.best_block);
    printf("transactions=%d\n", info.transactions);
    printf("txouts=%d\n", info.txouts);
    printf("total_amount=%d\n", info.total_amount);
    for (idx = 0; idx < info.txouts; idx++)
    {
      printf("utxo=%d\n", info.utxo[idx].txid);
    }

    err = client.gettxout(0, 0, &txInfo);
    if (err == -1)
    {
      fprintf(stderr, "wallet: gettxout failed\n");
      exit (1);
    }

    input_tx.txid = 99;
    input_tx.vout = 0; // This should always be 0 in our simplified implementation
    raw_transaction = client.createrawtransaction(input_tx, 123456, 12);
    if (raw_transaction == -1)
    {
      fprintf(stderr, "wallet: createrawtransaction failed\n");
      exit (1);
    }

    signed_transaction = client.signrawtransaction(raw_transaction, 1234567);
    if (signed_transaction == -1)
    {
      fprintf(stderr, "wallet: signrawtransaction failed\n");
      exit (1);
    }

    txid = client.sendrawtransaction(signed_transaction);
    if (txid == -1)
    {
      fprintf(stderr, "wallet: sendrawtransaction failed\n");
      exit (1);
    }
  }

};
