/****************************************************************************
*  Title: wallet.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Bitcoin wallet top-level behavior
****************************************************************************/

import "c_double_handshake";	// import the standard channel

behavior Wallet(i_receiver c_gettxout, i_receiver c_gettxoutsetinfo,
  i_sender c_createrawtransaction, i_sender c_sendrawtransaction,
  i_sender c_signrawtransaction)
{

  void main(void)
  {
  }

};
