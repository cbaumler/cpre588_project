#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";

import "sw_miner";
import "hw_miner";
import "stimulus";
import "monitor";

behavior Main(void) {
    
  c_double_handshake c_abort;
  c_double_handshake c_blk_hdr;
  c_double_handshake c_nonce;
  c_double_handshake c_perf;
  c_double_handshake c_profile;
  c_double_handshake c_reset;
                  
  SW_Miner    sw_miner(c_abort, 
                       c_blk_hdr, 
                       c_nonce);
                       
  HW_Miner    hw_miner(c_abort,
                       c_blk_hdr, 
                       c_nonce,
                       c_perf,                        
                       c_profile,
                       c_reset);
                       
  Stimulus stimulus(c_profile,
                    c_reset);
                          
  Monitor  monitor(c_perf);
  
  int main(void) {
   
  	printf("\nmain: all threads starting");      
    par {
  	  sw_miner.main();
  	  hw_miner.main();
  	  stimulus.main();
  	  monitor.main();
    }
  	printf("\nmain: all threads stopped");    
      
    return 0;
  } // end Main.main()
    
}; // end Main()
