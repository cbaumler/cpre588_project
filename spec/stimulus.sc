/****************************************************************************
*  Title: stimulus.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Sends transactions and parametric data to the design
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include "../config/hwconfig.h"  // Defines hardware config parameters

import "c_double_handshake";	// import the standard channel

behavior Stimulus(i_sender c_transaction_in, i_sender c_profile)
{
  void main (void)
  {
    FILE *fin;
    char name[MAX_NAME_LENGTH];
    int value, idx;
    HWConfig hwconfig;
    int *p_hwconfig;

    p_hwconfig = (int*)(&(hwconfig.clock));

    fin = fopen("../config/hardware.cfg", "r");

    if (fin != NULL)
    {
      printf("Hardware Configuration:\n");
      for (idx = 0; idx < NUM_HW_PARAMETERS; idx++)
      {
        if (fscanf(fin, "%s %d", &name, &value) == EOF)
        {
          fprintf(stderr, "Too few parameters in hardware.cfg\n");
          exit(1);
        }
        p_hwconfig[idx] = value;
        printf("%s=%d\n", name, value);
      }
    }
    else
    {
      fprintf(stderr, "Failed to read hardware.cfg\n");
      exit(1);
    }

    // Terminate the simulation after the desired amount of time
    waitfor(100); // TODO make simulation time configurable
    exit (0);
  }
};
