/****************************************************************************
*  Title: tb.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: test bench for Bitcoin miner
****************************************************************************/

import "design";
import "monitor";
import "stimulus";
import "c_double_handshake";	// import the standard double handshake channel

behavior Main
{
	c_double_handshake c_p2p_request;
	c_double_handshake c_p2p_response;
	c_double_handshake c_profile;
	c_double_handshake c_perf;
	c_double_handshake c_wallet_cmd;
	c_double_handshake c_wallet_log;
	c_double_handshake c_core_log;

	unsigned int mining_difficulty;

	Stimulus stimulus(c_p2p_request, c_p2p_response, c_profile, c_wallet_cmd,
	  mining_difficulty);
	Design design(c_p2p_request, c_p2p_response, c_profile, c_perf, c_wallet_cmd,
	  c_wallet_log, c_core_log, mining_difficulty);
	Monitor monitor(c_core_log, c_perf, c_wallet_log);

	int main (void)
	{
		par {
			stimulus.main();
			design.main();
			monitor.main();
		}

		return 0;
	}
};
