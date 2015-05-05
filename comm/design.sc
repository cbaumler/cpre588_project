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
import "tlmbus";

behavior Design(i_receiver c_p2p_request, i_sender c_p2p_response,
  i_receiver c_profile, i_sender c_perf, i_receiver c_wallet_cmd,
  i_sender c_wallet_log, i_sender c_core_log, in unsigned int mining_difficulty)
{
  // Channels

  // Wallet - Core RPC Channels
  c_double_handshake c_wallet_request;
  c_double_handshake c_wallet_response;

  // Buses
  TLMBus wallet_bus;
  TLMBus miner_bus;

  // Processing Elements

  PE1 pe1(c_wallet_request, c_wallet_response, wallet_bus,
    c_wallet_cmd, c_wallet_log);

  PE2 pe2(wallet_bus);

  PE3 pe3(c_wallet_request, c_wallet_response, c_p2p_request, c_p2p_response,
    mining_difficulty, c_core_log, miner_bus);

  PE4 pe4(miner_bus, c_perf, c_profile);

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
