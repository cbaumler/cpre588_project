/****************************************************************************
*  Title: pe3.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Processing Element 4
****************************************************************************/

import "c_double_handshake";	// import the standard double handshake channel
import "core";
import "miningsw";

behavior PE3(i_receiver c_wallet_request, i_sender c_wallet_response,
  i_receiver c_p2p_request, i_sender c_p2p_response,
  in unsigned int mining_difficulty, i_sender c_core_log, i_sender c_blk_hdr,
  i_receiver c_nonce, i_sender c_abort)
{

  // Software Miner - Core RPC Channels
  c_double_handshake c_swminer_request;
  c_double_handshake c_swminer_response;

  Core core(c_wallet_request, c_wallet_response, c_swminer_request,
    c_swminer_response, c_p2p_request, c_p2p_response, mining_difficulty, c_core_log);

  MiningSW miningsw(c_swminer_request, c_swminer_response, c_blk_hdr, c_nonce, c_abort);

  void main (void)
  {
    par {
      core.main();
      miningsw.main();
    }
  }

};
