/****************************************************************************
*  Title: pe2.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Processing Element 2
****************************************************************************/

import "c_double_handshake";	// import the standard double handshake channel
import "hardwarewallet";

behavior PE2(i_receiver c_hw_wallet_in, i_sender c_hw_wallet_out)
{

  HardwareWallet hw_wallet(c_hw_wallet_in, c_hw_wallet_out);

  void main (void)
  {
    hw_wallet.main();
  }

};
