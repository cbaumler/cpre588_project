/****************************************************************************
*  Title: hardwareWallet.sc
*  Author: Team 4
*  Date:
*  Description: The hardware wallet is deidicated to running the signing-only wallet
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>

import "networkWallet";
import "c_double_handshake";    // import standard channel
import "rpcclient";
import "core";"


behavior hardwareWallet(i_receiver c_hw_wallet_in, i_sender c_hw_wallet_out)
{

// Declarations

int parentPublicKey, parentPrivateKey;
unsigned char blockhash, details, signedMessage;
int err, idx, txid;
TxOutSetInfo info;
TxOut txInfo;
Outpoint input_tx;
int raw_transaction, signed_transaction;


RPCClient client(c_request, c_response);


void main(void) {

// Step 1. Interact with the peer-to-peer network to get info from blockchain

        blockHash = CoreInit(); //identify hash
        block = client.call('getblock', blockHash);
        details = client.call('gettransaction', txid); // transaction details

// Step 2. create parent public key - a point on curve definied in secp256k1
// For purposes of this project, this key will be hard-coded

parentPublicKey = block; // bitcoin address is hash of public key

// Step 3. Create parent private key - 256-bit number (secp256k1 ECDSA encryption)
// For purposes of this project, this key will be hard-coded

        parentPrivateKey = rand() % 7 + 1;

// Step 9. Sign transactions

        //signedMessage = client.call('signmessage', block);
        signed_transaction = client.signrawtransaction(raw_transaction, 1234567);
        if (signed_transaction == -1)
        {
            fprintf(stderr, "wallet: signrawtransaction failed\n");
            exit (1);
        }

// Step 10. Send signed transaction to networkWallet
// I'm assuming this can be called and from networkWallet and sent to peer-to-peer?

};
