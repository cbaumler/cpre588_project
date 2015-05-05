/****************************************************************************
*  Title: monitor.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Writes system performance data to a log
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include "../api/coreapi.h"
#include "../api/testapi.h"

import "c_double_handshake";	// import the standard double handshake channel

behavior PerformanceMonitor (i_receiver c_perf)
{

  void main (void)
  {
    FILE *fout;
    PerformanceData perf_data;
    int idx;
    int hashrate, energy_efficiency, cost_efficiency;

    // Open the output log file
    fout = fopen("../log/performance.log", "w");

    while (true)
    {
      // Receive the performance data
      c_perf.receive(&perf_data, sizeof(perf_data));
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

      // Write code performance data to log
      fprintf(fout, "\nCode Performance Breakdown:\n\n");
      for (idx = 0; idx < NUM_CODE_BLOCKS; idx++)
      {
        fprintf(fout, "Code Block  %2d: Time=%10d  Power=%10d\n", idx+1,
          perf_data.codeblocks[idx].time,
          perf_data.codeblocks[idx].power);
      }

      // Write block performance data to log
      fprintf(fout, "\nBlock Performance Breakdown:\n\n");
      fprintf(fout, "Total Blocks Mined: %d\n\n", perf_data.num_mined_blocks);
      for (idx = 0; idx < perf_data.num_mined_blocks; idx++)
      {
        fprintf(fout, "Mined Block: %2d: Time=%10d  Power=%10d\n", idx+1,
          perf_data.mined_blocks[idx].time,
          perf_data.mined_blocks[idx].power);
      }

      // Write overall performance data to log
      hashrate = perf_data.total_num_hashes / perf_data.total_sim_time;
      energy_efficiency = hashrate / perf_data.total_power;
      cost_efficiency = hashrate / perf_data.total_cost;

      fprintf(fout, "\nOverall Performance:\n\n");
      fprintf(fout, "Total Power Usage : %10d Watts\n", perf_data.total_power);
      fprintf(fout, "Total Cost        : %10d Dollars\n", perf_data.total_cost);
      fprintf(fout, "Hash Rate         : %10d Hashes/Second\n", hashrate);
      fprintf(fout, "Energy Efficiency : %10d Hashes/Joule\n", energy_efficiency);
      fprintf(fout, "Cost Efficiency   : %10d Hashes/Second/Dollar\n", cost_efficiency);

      fflush(fout);
    }
  }
};

behavior CoreMonitor (i_receiver c_core_log)
{

  void main (void)
  {
    char log_msg[MAX_CORE_LOG_MSG_SIZE];
    FILE *fout;

    // Open the output log
    fout = fopen("../log/core.log", "w");

    while (true)
    {
      // Receive log messages from the Bitcoin core
      c_core_log.receive(log_msg, sizeof(log_msg));

      // Write messages to the log
      fprintf(fout, "%s", log_msg);
      fflush(fout);
    }
  }
};

behavior WalletMonitor (i_receiver c_wallet_log)
{

  void main (void)
  {
    char log_msg[MAX_WALLET_LOG_MSG_SIZE];
    FILE *fout;

    // Open the output log
    fout = fopen("../log/wallet.log", "w");

    while (true)
    {
      // Receive log messages from the wallet
      c_wallet_log.receive(log_msg, sizeof(log_msg));

      // Write messages to the log
      fprintf(fout, "%s", log_msg);
      fflush(fout);
    }
  }
};

behavior Monitor (i_receiver c_core_log, i_receiver c_performance,
  i_receiver c_wallet_log)
{
  PerformanceMonitor perf_mon(c_performance);
  CoreMonitor core_mon(c_core_log);
  WalletMonitor wallet_mon(c_wallet_log);

  void main (void)
  {
    par {
      perf_mon.main();
      core_mon.main();
      wallet_mon.main();
    }
  }

};
