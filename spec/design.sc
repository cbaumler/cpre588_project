/****************************************************************************
*  Title: design.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Top level design behavior for the Bitcoin miner
****************************************************************************/

import "wallet";
import "core";
import "miningsw";
import "mininghw";
import "c_double_handshake";	// import the standard channel

behavior Design(i_receiver c_transaction_in, i_sender c_transaction_out,
  i_receiver c_profile, i_sender c_performance)
{
  // Channels

  // Wallet - Core RPC Channels
  c_double_handshake c_wallet_request;
  c_double_handshake c_wallet_response;

  // Software Miner - Core RPC Channels
  c_double_handshake c_swminer_request;
  c_double_handshake c_swminer_response;

  // Software Miner - Hardware Miner Channels
  c_double_handshake c_blk_hdr;
  c_double_handshake c_nonce;

  // Behaviors

  Wallet wallet(c_wallet_request, c_wallet_response);
  Core core(c_wallet_request, c_wallet_response, c_swminer_request,
    c_swminer_response, c_transaction_in, c_transaction_out);
  MiningSW miningsw(c_swminer_request, c_swminer_response, c_blk_hdr, c_nonce);
  MiningHW mininghw(c_blk_hdr, c_nonce, c_profile, c_performance);

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
