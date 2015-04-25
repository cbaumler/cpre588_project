/****************************************************************************
*  Title: core.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Core Bitcoin protocol behavior
****************************************************************************/

import "c_double_handshake";	// import the standard channel

behavior Core(i_sender c_gettxout, i_sender c_gettxoutsetinfo,
  i_receiver c_createrawtransaction, i_receiver c_sendrawtransaction,
  i_receiver c_signrawtransaction, i_sender c_getblocktemplate,
  i_receiver c_submitblock, i_sender c_getblockcount, i_receiver c_tx_in,
  i_sender c_tx_out)
{

  void main(void)
  {
  }

};
