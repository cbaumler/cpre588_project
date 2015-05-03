/****************************************************************************
*  Title: design.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Top level design behavior for the Bitcoin miner
****************************************************************************/

import "wallet";
import "core";
import "miningsw";
import "hw_miner";
import "c_double_handshake";	// import the standard channel

behavior Design(i_receiver c_p2p_request, i_sender c_p2p_response,
  i_receiver c_profile, i_sender c_perf)
{
  // Channels

  // Wallet - Core RPC Channels
  c_double_handshake c_wallet_request;
  c_double_handshake c_wallet_response;

  // Software Miner - Core RPC Channels
  c_double_handshake c_swminer_request;
  c_double_handshake c_swminer_response;

  // Software Miner - Hardware Miner Channels
  c_double_handshake c_abort;
  c_double_handshake c_blk_hdr;
  c_double_handshake c_nonce;
  c_double_handshake c_reset;

  // Behaviors

  Wallet wallet(c_wallet_request, c_wallet_response);
  Core core(c_wallet_request, c_wallet_response, c_swminer_request,
    c_swminer_response, c_p2p_request, c_p2p_response);
  MiningSW miningsw(c_swminer_request, c_swminer_response, c_blk_hdr, c_nonce);
  HW_Miner mininghw(c_abort, c_blk_hdr, c_nonce, c_perf, c_profile, c_reset);

  void main(void)
  {
    par {
			wallet.main();
			core.main();
			miningsw.main();
      mininghw.main();
		}
  }

};
