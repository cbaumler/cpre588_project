#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel

behavior HW_Config(i_receiver c_profile,
                   i_receiver c_reset,
                   event      e_ready,
                   event      e_reset) {

  int temp;
  
	void main(void) {
		while(true) {
		  c_profile.receive(&temp, sizeof(temp));		
		  notify(e_ready);
		  c_reset.receive(&temp, sizeof(temp));
		  notify(e_reset);
	  }
	} // end HW_Config.main()
	
}; // end HW_Config()

