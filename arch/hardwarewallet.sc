/****************************************************************************
*  Title: hardwarewallet.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Wallet component that signs transactions
****************************************************************************/

import "c_double_handshake";	// import the standard channel

behavior HardwareWallet (i_receiver c_hw_wallet_in, i_sender c_hw_wallet_out)
{
  void main (void)
  {
    bool request_key;
    int key = 1234567;

    // Wait for a request for a key
    c_hw_wallet_in.receive(&request_key, sizeof(request_key));

    // Send a key
    c_hw_wallet_out.send(&key, sizeof(key));
  }
};
