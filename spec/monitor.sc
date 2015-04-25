/****************************************************************************
*  Title: monitor.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Writes system performance data to a log
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel

behavior Monitor(i_receiver c_profile, i_receiver c_performance)
{

  void main(void)
  {
    FILE *fout;

    fout = fopen("../log/performance.log", "w");

    fprintf(fout, "%s\n", "Performance data will go here\n");
    exit(0);
  }

};
