#include <stdlib.h>
#include <stdio.h>
#include "../config/hwconfig.h" // Defines hardware config parameters

import "c_double_handshake";	// import the standard channel
import "tlmbus";

behavior HW_Config(i_receiver      c_profile,
                   IProtocolSlave  miner_bus,
                   event           e_ready,
                   event           e_reset) {

	void main(void) {
    int temp;
    HWConfig hwconfig;

		while(true) {
			printf("DEBUG-hw_config:c_profile.receive(), hwconfig size = %d\n", sizeof(hwconfig));
		  c_profile.receive(&hwconfig, sizeof(hwconfig)); // TODO: Use the hardware config
		  printf("DEBUG-hw_config:profile received\n");
		  notify(e_ready);
      miner_bus.slaveRead(C4, &temp, sizeof(temp));
		  notify(e_reset);
	  }
	} // end HW_Config.main()

}; // end HW_Config()
