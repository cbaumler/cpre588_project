/****************************************************************************
*  Title: tb.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: test bench for Bitcoin miner
****************************************************************************/

import "design";
import "monitor";
import "stimulus";
import "c_double_handshake";	// import the standard channel

behavior Main
{
	c_double_handshake c_transaction_in;
	c_double_handshake c_transaction_out;
	c_double_handshake c_profile;
	c_double_handshake c_performance;

	Stimulus stimulus(c_transaction_in, c_profile);
	Design design(c_transaction_in, c_transaction_out, c_profile, c_performance);
	Monitor monitor(c_transaction_out, c_performance);

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
