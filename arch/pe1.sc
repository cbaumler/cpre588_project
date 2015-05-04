/****************************************************************************
*  Title: pe1.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Processing Element 1
****************************************************************************/

import "c_double_handshake";	// import the standard double handshake channel
import "networkwallet";

behavior PE1(i_sender c_request, i_receiver c_response,
  i_sender c_hw_wallet_in, i_receiver c_hw_wallet_out, i_receiver c_wallet_cmd,
  i_sender c_wallet_log)
{

  NetworkWallet net_wallet(c_request, c_response, c_hw_wallet_in,
    c_hw_wallet_out, c_wallet_cmd, c_wallet_log);

  void main(void)
  {
    net_wallet.main();
  }

};
