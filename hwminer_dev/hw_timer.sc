#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel

behavior Wait_For_Timeout(event e_tout) {
	void main(void) {
		waitfor(1000);
		notify(e_tout);
	}
};

behavior HW_Timer(event    e_tout,
                  event    e_tset) {

  Wait_For_Timeout wait_for_timeout(e_tout);
  
  void main(void) {
    while(true) {
    	wait(e_tset);
		  try          {wait_for_timeout.main();}
		  trap(e_tset) {}
    }
    
  } // end HW_Timer.main()
  
}; // end HW_Timer()

