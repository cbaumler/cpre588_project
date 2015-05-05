/****************************************************************************
*  Title: hw_config.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Accepts PE configuration data from testbench and forwards
*    it to hashing algorithm for measurements.
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include "../config/hwconfig.h" // Defines hardware config parameters

import "c_double_handshake";	// import the standard channel

behavior HW_Config(i_receiver c_profile,
                   i_sender   c_pe_profile,
                   i_receiver c_reset,
                   event      e_reset) {

	void main(void) {
    int temp;
    HWConfig hwconfig;

		while(true) {
			printf("HW_Config:c_profile.receive(), hwconfig size = %d\n", sizeof(hwconfig));
		  c_profile.receive(&hwconfig, sizeof(hwconfig));
		  printf("HW_Config:profile received, forwarding to HW_Hash\n");
		  c_pe_profile.send(&hwconfig, sizeof(hwconfig));
		  printf("HW_Config:c_reset.receive()\n");
		  c_reset.receive(&temp, sizeof(temp));
		  notify(e_reset);
	  }
	} // end HW_Config.main()

}; // end HW_Config()
