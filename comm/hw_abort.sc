/****************************************************************************
*  Title: hw_abort.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description:
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>

import "tlmbus";

behavior HW_Abort(IProtocolSlave miner_bus,
                  event          e_abort) {

  int command;

  void main(void) {
    while (true) {
      miner_bus.slaveRead(C1, &command, sizeof(command));
		  notify(e_abort);
    }

  } // end HW_Abort.main()

}; // end HW_Abort()
