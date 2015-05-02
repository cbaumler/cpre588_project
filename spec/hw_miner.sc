#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel

import "hw_hash";
import "hw_abort";
import "hw_timer";
import "hw_config";
   
event e_reset;
event e_ready;
event e_abort;
event e_tout;
event e_tstart;
event e_tstop;

behavior HW_Miner(i_receiver   c_abort,
                  i_receiver   c_blk_hdr, 
                  i_sender     c_nonce,
                  i_sender     c_perf,
                  i_receiver   c_profile,
                  i_receiver   c_reset) {
  	                      
  HW_Hash hw_hash(c_blk_hdr, 
	                c_nonce, 
	                c_perf,
	                e_abort,
	                e_ready,
	                e_reset,
	                e_tout,
	                e_tstart,
	                e_tstop);  	                      
	  
  HW_Config hw_config(c_profile,
                      c_reset,
                      e_ready,
                      e_reset); 	  
	                      
  HW_Abort hw_abort(c_abort,
                    e_abort);  
      
  HW_Timer hw_timer(e_tout,
                    e_tstart,
                    e_tstop); 
                    
                                              
  void main(void) {	                      
  	      
  	printf("\nHW_Miner:par{}");                         	
  	par {
  	  hw_config.main();
  	  hw_abort.main();
  	  hw_timer.main();
  	  hw_hash.main();	
    }
    printf("\nHW_Miner:threads stopped");  	
    
  } // end HW_Miner.main()

}; // end HW_Miner()
