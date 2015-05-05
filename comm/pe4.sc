/****************************************************************************
*  Title: pe4.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Processing Element 4
****************************************************************************/

import "c_double_handshake";	// import the standard double handshake channel
import "hw_miner";
import "tlmbus";

behavior PE4(IProtocolSlave miner_bus, i_sender c_perf, i_receiver c_profile)
{
  HW_Miner mininghw(miner_bus, c_perf, c_profile);

  void main (void)
  {
    mininghw.main();
  }

};
