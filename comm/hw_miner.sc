/****************************************************************************
*  Title: hw_miner.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Outer encapsulation of hardware miner behaviors.
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel
import "tlmbus";

import "hw_hash";
import "hw_abort";
import "hw_timer";
import "hw_config";

event e_reset;
event e_abort;
event e_tout;
event e_tstart;
event e_tstop;

behavior HW_Miner(IProtocolSlave miner_bus,
                  i_sender       c_perf,
                  i_receiver     c_profile) {

  c_double_handshake c_pe_profile;

  HW_Hash hw_hash(miner_bus,
	                c_perf,
                  c_pe_profile,
	                e_abort,
	                e_reset,
	                e_tout,
	                e_tstart,
	                e_tstop);

  HW_Config hw_config(c_profile,
                      c_pe_profile,
                      miner_bus,
                      e_reset);

  HW_Abort hw_abort(miner_bus,
                    e_abort);

  HW_Timer hw_timer(e_tout,
                    e_tstart,
                    e_tstop);


  void main(void) {

  	par {
  	  hw_config.main();
  	  hw_abort.main();
  	  hw_timer.main();
  	  hw_hash.main();
    }

  } // end HW_Miner.main()

}; // end HW_Miner()
