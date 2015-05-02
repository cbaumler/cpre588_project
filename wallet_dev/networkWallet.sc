/****************************************************************************
*  Title: networkWallet.sc
*  Author: Team 4
*  Date:
*  Description: The network wallet works in conjunction with the signing-only wallet
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>

import "hardwareWallet";         // signing-only implemented as hardware wallet
import "c_double_handshake";    // import standard channel
import "rpcclient";

/**************** Bitcoin Transaction Framework **********************

*   An input - this is a record of which bitcoin address that was used to send the bitcoins
*   An amount - this is the amount of bitcoins that is being sent
*   An output - this is the bitcoin owner's address
*   Transaction needs to be signed
*   Transaction needs to be submitted to the network

**************** Bitcoin Transaction Framework **********************/


behavior networkWallet(i_sender c_request, i_receiver c_response,
                        i_sender c_hw_wallet_in, i_receiver c_hw_wallet_out)
{

//Declarations

int bal, showBalance;
int childPublicKey;
int parentPublicKey, parentPrivateKey;
int err, idx, txid;
TxOutSetInfo info;
TxOut txInfo;
Outpoint input_tx;
int raw_transaction, signed_transaction;

RPCClient client(c_request, c_response);

void main(void) {


// Step 4. Use parent public key to derive child public keys

        parentPublicKey = childPublicKey;

// Step 5. Attach the parent chain code, parent public key, and index number to the hash
// The code below is the right framework, but need associated library to reference 

        point((parentPrivateKey + lefthand_hash_output) % G) == child_public_key
        point(childPrivateKey) == parentPublicKey + point(lefthand_hash_output)

// Step 6. Monitor for outputs spent to the public keys

        verify outputs (bitcion owner address) when coins are sent

// Step 7. Create unsigned transactions spending the outputs

// Step 8. Transfer the unsigned transactions to the signing-only wallet (hardware)

// Receieve and verify the signed transactions from hardwareWallet

        Use API call "verifymessage" to verify transaction

// Step 11. Submit transaction to the network

        txid = client.sendrawtransaction(signed_transaction);
        if (txid == -1)
        {
            fprintf(stderr, "wallet: sendrawtransaction failed\n");
            exit (1);
        }
    }
};

//************************Alternative Implementations*******************


// Track bitcoin balance

Look into the blockchain and identify particular address
bal = client.getbalance(&showBalance);
printf(stderr, "account balance is: \n", showBalance);

// Create raw transaction

            raw_transaction = client.createrawtransaction(input_tx, 123456, 12);
            if (raw_transaction == -1)
            {
                fprintf(stderr, "wallet: createrawtransaction failed\n");
                exit (1);
            }

// Call gettxoutsetinfo() API to get list of unspent transactions - example code below

Value gettxoutsetinfo(const Array& params, bool fHelp)
{
    if (fHelp || params.size() != 0)
        throw runtime_error(
            "gettxoutsetinfo\n"
            "\nReturns statistics about the unspent transaction output set.\n"
            "Note this call may take some time.\n"
            "\nResult:\n"
            "{\n"
            "  \"height\":n,     (numeric) The current block height (index)\n"
            "  \"bestblock\": \"hex\",   (string) the best block hash hex\n"
            "  \"transactions\": n,      (numeric) The number of transactions\n"
            "  \"txouts\": n,            (numeric) The number of output transactions\n"
            "  \"bytes_serialized\": n,  (numeric) The serialized size\n"
            "  \"hash_serialized\": \"hash\",   (string) The serialized hash\n"
            "  \"total_amount\": x.xxx          (numeric) The total amount\n"
            "}\n"
            "\nExamples:\n"
            + HelpExampleCli("gettxoutsetinfo", "")
            + HelpExampleRpc("gettxoutsetinfo", "")
        );

// Another implementation of gettxoutsetinfo

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

// Check unspent transaction outputs. See if any addreses belong to user

        err = client.gettxout(0, 0, &txInfo);
            if (err == -1)
            {
                fprintf(stderr, "wallet: gettxout failed\n");
                exit (1);
            }

                input_tx.txid = 99;
                input_tx.vout = 0; // This should always be 0 in our simplified implementation

// Keep track of transaction and the number of Bitcoins the user recieved







