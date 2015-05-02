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
import "networkwallet";
import "hardwarewallet";

behavior Wallet(i_sender c_request, i_receiver c_response)
{
  RPCClient client(c_request, c_response);

  //Declarations of channels here....eg unsigned int iStart etc..
  c_double_handshake c_hw_wallet_in;
  c_double_handshake c_hw_wallet_out;

  NetworkWallet U00(c_request, c_response, c_hw_wallet_in, c_hw_wallet_out);
  HardwareWallet U01(c_hw_wallet_in, c_hw_wallet_out);

  void main(void)
  {
      par {
          U00.main();
          U01.main();
      }
  }
};
