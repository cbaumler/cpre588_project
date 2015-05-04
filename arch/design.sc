/****************************************************************************
*  Title: design.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Top level design behavior for the Bitcoin miner
****************************************************************************/

#include <stdio.h>

import "c_double_handshake";	// import the standard double handshake channel
import "pe1";
import "pe2";
import "pe3";
import "pe4";

behavior Design(i_receiver c_p2p_request, i_sender c_p2p_response,
  i_receiver c_profile, i_sender c_perf, i_receiver c_wallet_cmd,
  i_sender c_wallet_log, i_sender c_core_log, in unsigned int mining_difficulty)
{
  // Channels

  // Wallet - Core RPC Channels
  c_double_handshake c_wallet_request;
  c_double_handshake c_wallet_response;

  // Software Miner - Hardware Miner Channels
  c_double_handshake c_abort;
  c_double_handshake c_blk_hdr;
  c_double_handshake c_nonce;
  c_double_handshake c_reset;

  // Network Wallet - Hardware Wallet Channels
  c_double_handshake c_hw_wallet_in;
  c_double_handshake c_hw_wallet_out;

  // Processing Elements

  PE1 pe1(c_wallet_request, c_wallet_response, c_hw_wallet_in, c_hw_wallet_out,
    c_wallet_cmd, c_wallet_log);

  PE2 pe2(c_hw_wallet_in, c_hw_wallet_out);

  PE3 pe3(c_wallet_request, c_wallet_response, c_p2p_request, c_p2p_response,
    mining_difficulty, c_core_log, c_blk_hdr, c_nonce, c_abort);

  PE4 pe4(c_abort, c_blk_hdr, c_nonce, c_perf, c_profile, c_reset);

  void main(void)
  {
    par {
			pe1.main();
      pe2.main();
      pe3.main();
      pe4.main();
		}
  }

};
