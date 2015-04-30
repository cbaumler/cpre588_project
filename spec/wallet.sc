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
import "networkWallet";
import "hardwareWallet";

behavior Wallet(i_sender c_request, i_receiver c_response)
{
  RPCClient client(c_request, c_response);

  //Declarations of channels here....eg unsigned int iStart etc..

  networkWallet U00(Inputs and Ouputs);
  hardwareWallet U01(Inputs and Outputs);

  void main(void)
      par {
          U00.main();
          U01.main();
      }
  }
};

