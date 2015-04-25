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

behavior Design(i_receiver core_in, i_sender core_out,
  i_receiver mininghw_in, i_sender mininghw_out)
{
  // Channels

  // Wallet - Core Channels
  c_double_handshake c_gettxout;
  c_double_handshake c_gettxoutsetinfo;
  c_double_handshake c_createrawtransaction;
  c_double_handshake c_sendrawtransaction;
  c_double_handshake c_signrawtransaction;

  // Core - Software Miner Channels
  c_double_handshake c_getblocktemplate;
  c_double_handshake c_submitblock;
  c_double_handshake c_getblockcount;

  // Software Miner - Hardware Miner Channels
  c_double_handshake c_blk_hdr;
  c_double_handshake c_nonce;

  // Stimulus - Design Channels
  c_double_handshake c_tx_in;
  c_double_handshake c_profile;

  // Design - Monitor Channels
  c_double_handshake c_tx_out;
  c_double_handshake c_performance;

  // Behaviors

  Wallet wallet(c_gettxout, c_gettxoutsetinfo, c_createrawtransaction,
    c_sendrawtransaction, c_signrawtransaction);

  Core core(c_gettxout, c_gettxoutsetinfo, c_createrawtransaction,
    c_sendrawtransaction, c_signrawtransaction, c_getblocktemplate,
    c_submitblock, c_getblockcount, c_tx_in, c_tx_out);

  MiningSW miningsw(c_blk_hdr, c_nonce);

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
