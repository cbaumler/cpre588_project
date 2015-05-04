/****************************************************************************
*  Title: pe4.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Processing Element 4
****************************************************************************/

import "c_double_handshake";	// import the standard double handshake channel
import "hw_miner";

behavior PE4(i_receiver c_abort, i_receiver c_blk_hdr, i_sender c_nonce,
  i_sender c_perf, i_receiver c_profile, i_receiver c_reset)
{
  HW_Miner mininghw(c_abort, c_blk_hdr, c_nonce, c_perf, c_profile, c_reset);

  void main (void)
  {
    mininghw.main();
  }

};
