/****************************************************************************
*  Title: pe2.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Processing Element 2
****************************************************************************/

import "tlmbus";
import "hardwarewallet";

behavior PE2(IProtocolSlave wallet_bus)
{

  HardwareWallet hw_wallet(wallet_bus);

  void main (void)
  {
    hw_wallet.main();
  }

};
