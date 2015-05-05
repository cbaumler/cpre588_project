/****************************************************************************
*  Title: hardwarewallet.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Wallet component that signs transactions
****************************************************************************/

import "c_double_handshake";	// import the standard channel
import "tlmbus";

behavior HardwareWallet (IProtocolSlave wallet_bus)
{
  void main (void)
  {
    bool request_key;
    int key = 1234567;

    // Wait for a request for a key
    wallet_bus.slaveRead(C1, &request_key, sizeof(request_key));

    // Send a key
    wallet_bus.slaveWrite(C2, &key, sizeof(key));
  }
};
