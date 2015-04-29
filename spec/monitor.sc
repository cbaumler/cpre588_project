/****************************************************************************
*  Title: monitor.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Writes system performance data to a log
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";	// import the standard channel

#define NUM_CODE_BLOCKS   26
#define MAX_MINED_BLOCKS  48

typedef struct
{
  int time;
  int power;

} Stats;

typedef struct
{
  Stats codeblocks[NUM_CODE_BLOCKS];
  Stats mined_blocks[MAX_MINED_BLOCKS];
  int num_mined_blocks;
  int total_power;
  int total_num_hashes;
  int total_sim_time;
  int total_cost;

} PerformanceData;

behavior PerformanceMonitor (i_receiver c_performance)
{

  void main (void)
  {
    FILE *fout;
    PerformanceData perf_data;
    int idx;
    int hashrate, energy_efficiency, cost_efficiency;

    // Receive the performance data
    //c_performance.receive(&perf_data, sizeof(perf_data));
    for (idx = 0; idx < NUM_CODE_BLOCKS; idx++)
    {
      perf_data.codeblocks[idx].time = idx;
      perf_data.codeblocks[idx].power = idx;
      perf_data.mined_blocks[idx].time = idx;
      perf_data.mined_blocks[idx].power = idx;
      perf_data.num_mined_blocks = NUM_CODE_BLOCKS;
      perf_data.total_power = 360;
      perf_data.total_sim_time = 1;
      perf_data.total_num_hashes = 180000000;
      perf_data.total_cost = 299;
    }

    // Write the performance data to a log
    fout = fopen("../log/performance.log", "w");

    // Write code performance data
    fprintf(fout, "\nCode Performance Breakdown:\n\n");
    for (idx = 0; idx < NUM_CODE_BLOCKS; idx++)
    {
      fprintf(fout, "Code Block  %2d: Time=%10d  Power=%10d\n", idx+1,
        perf_data.codeblocks[idx].time,
        perf_data.codeblocks[idx].power);
    }

    // Write block performance data
    fprintf(fout, "\nBlock Performance Breakdown:\n\n");
    fprintf(fout, "Total Blocks Mined: %d\n\n", perf_data.num_mined_blocks);
    for (idx = 0; idx < perf_data.num_mined_blocks; idx++)
    {
      fprintf(fout, "Mined Block: %2d: Time=%10d  Power=%10d\n", idx+1,
        perf_data.mined_blocks[idx].time,
        perf_data.mined_blocks[idx].power);
    }

    // Write overall performance data
    hashrate = perf_data.total_num_hashes / perf_data.total_sim_time;
    energy_efficiency = hashrate / perf_data.total_power;
    cost_efficiency = hashrate / perf_data.total_cost;

    fprintf(fout, "\nOverall Performance:\n\n");
    fprintf(fout, "Total Power Usage : %10d Watts\n", perf_data.total_power);
    fprintf(fout, "Total Cost        : %10d Dollars\n", perf_data.total_cost);
    fprintf(fout, "Hash Rate         : %10d Hashes/Second\n", hashrate);
    fprintf(fout, "Energy Efficiency : %10d Hashes/Joule\n", energy_efficiency);
    fprintf(fout, "Cost Efficiency   : %10d Hashes/Second/Dollar\n", cost_efficiency);
  }
};

behavior CoreMonitor (i_receiver c_transaction_out)
{

  void main (void)
  {

  }
};

behavior Monitor (i_receiver c_transaction_out, i_receiver c_performance)
{
  PerformanceMonitor perf_mon(c_performance);
  CoreMonitor core_mon(c_transaction_out);

  void main (void)
  {
    par {
      perf_mon.main();
      core_mon.main();
    }

  }

};
