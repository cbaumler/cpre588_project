/****************************************************************************
*  Title: hw_abort.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Accepts abort command vis channel and issues abort
*    notification to halt any in-progress hashing.
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel

behavior HW_Abort(i_receiver c_abort,
                  event      e_abort) {

  int command;
  
  void main(void) {
    while (true) {
		  c_abort.receive(&command, sizeof(command));
		  notify(e_abort);
    }
    
  } // end HW_Abort.main()
  
}; // end HW_Abort()

