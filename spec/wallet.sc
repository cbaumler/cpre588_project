/****************************************************************************
*  Title: wallet.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Bitcoin wallet top-level behavior
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include "../api/coreapi.h"

import "c_double_handshake";	// import the standard channel
import "rpcclient";
import "networkwallet";
import "hardwarewallet";

behavior Wallet(i_sender c_request, i_receiver c_response, i_receiver c_spend,
  in event e_log_wallet, i_sender c_wallet_log)
{
  RPCClient client(c_request, c_response);

  //Declarations of channels here....eg unsigned int iStart etc..
  c_double_handshake c_hw_wallet_in;
  c_double_handshake c_hw_wallet_out;

  NetworkWallet net_wallet(c_request, c_response, c_hw_wallet_in,
    c_hw_wallet_out, c_spend, e_log_wallet, c_wallet_log);
  HardwareWallet hw_wallet(c_hw_wallet_in, c_hw_wallet_out);

  void main(void)
  {
      par {
          net_wallet.main();
          hw_wallet.main();
      }
  }
};
