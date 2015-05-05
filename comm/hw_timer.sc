/****************************************************************************
*  Title: hw_timer.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Serves as watchdog timer to halt in-progress hashing if it
*    runs longer than a configurable threshold.
*
*  NOTE:  Incomplete.  This behavior is instantiated and executed, but 
*    currently does not generate a timeout notification due to an unresolved
*    interface issue between the HW_Miner and SW_Miner packages.
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel

behavior Wait_For_Timeout(event e_tout) {
	void main(void) {
		waitfor(5000000000);
		//notify(e_tout);
	}
};

behavior HW_Timer(event    e_tout,
                  event    e_tstart,
                  event    e_tstop) {

  Wait_For_Timeout wait_for_timeout(e_tout);
  
  void main(void) {
    while(true) {
    	wait(e_tstart);
		  try           {wait_for_timeout.main();}
		  trap(e_tstop) {}
    }
    
  } // end HW_Timer.main()
  
}; // end HW_Timer()

