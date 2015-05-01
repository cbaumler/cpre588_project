#include <sim.sh>
#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel

behavior Monitor(i_receiver c_perf) {

typedef struct {
	unsigned long sim_time;
	double        sim_power;
} Perf_Data;

Perf_Data perf_data;

  void main(void) {
    FILE *fout;
    fout = fopen("performance.log", "w");
    fprintf(fout, "%s\n", "Perf log opened\n");
    
    while (true) {
       c_perf.receive(&perf_data, sizeof(perf_data));
       printf("time=%lu, power=%f6.1", perf_data.sim_time, perf_data.sim_power);
    }
  }
};