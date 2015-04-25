/****************************************************************************
*  Title: core.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Core Bitcoin protocol behavior
****************************************************************************/

#include "coreapi.h"

import "c_double_handshake";	// import the standard channel

behavior Core(i_sender c_wallet_in, i_receiver c_wallet_out,
  i_sender c_swminer_in, i_receiver c_swminer_out,
  i_receiver c_transaction_in, i_sender c_transaction_out)
{

  void main(void)
  {
  }

};
