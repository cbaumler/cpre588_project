#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel

behavior Wait_For_Timeout(event e_tout) {
	void main(void) {
		waitfor(5000000000);
		notify(e_tout);
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

