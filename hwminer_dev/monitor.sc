#include <sim.sh>
#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel

behavior Monitor(i_receiver c_perf) {

typedef struct {
	
	// Time statistics
  long long time_total;     // Time since start of simulation
  long long time_blk_start; // Time at start of block processing
  long long time_blk_stop;  // Time at end of current block processing
  long long time_blk;       // Time for processing current block
  
  // Hash count statistics
  long hash_total;          // Total hash count since start of simulation
  long hash_blk;            // Current hash count accumulator for current block
  
  // Power statistics
  long power_total;         // Total power since start of simulation
  long power_blk;           // Current power accumulator for current block
  
} Statistics;

Statistics perf_data;

  void main(void) {
    FILE *fout;
    fout = fopen("performance.log", "w");
    fprintf(fout, "%s\n", "Perf log opened\n");
    
    while (true) {
       c_perf.receive(&perf_data, sizeof(perf_data));
       //printf("time=%lu, power=%f6.1", perf_data.sim_time, perf_data.sim_power);
    }
  }
};