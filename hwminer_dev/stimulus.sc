#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel

behavior Stimulus(i_sender  c_profile,
                  i_sender  c_reset) {           	
  void main (void) {
  	int temp;
  	while(true) {
  		printf("\nStimulus:c_profile.send()");
      c_profile.send(&temp, sizeof(temp));
      printf("\nStimulus:c_reset.receive()");
      //c_reset.send(&temp, sizeof(temp));
      break;
    }
  }
};