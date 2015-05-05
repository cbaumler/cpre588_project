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
    
    PerformanceData perf;
    PerformanceData past_perf;
        
    int idx;
    int hashrate, energy_efficiency, cost_efficiency;

    // Open the output log file
    fout = fopen("../log/performance.log", "w");

    while (true)
    {
      // Receive the performance data
      c_perf.receive(&perf, sizeof(perf));
      
      if (perf.flag == 0) {
      	
        printf("\n\n"); 
        printf("================ QUICK LOOK ================\n");      
        printf("Monitor:cum blocks    = %g\n", perf.cum_blocks);                     
        printf("Monitor:MH/j          = %g\n", perf.mhash_per_j); 
        printf("Monitor:MH/s          = %g\n\n", perf.mhash_per_s);  
  
        fprintf(fout, "\n\n");
        fprintf(fout, "=============== BLOCK RECORD ===============\n");
        fprintf(fout, "== Block Number = %g\n", perf.cum_blocks);          
        fprintf(fout, "== Block Hashes = %g\n", 
          perf.cum_hashes - past_perf.cum_hashes);        
        fprintf(fout, "== Block Energy = %g joules\n", 
          perf.cum_energy - past_perf.cum_energy);  
        fprintf(fout, "== Block Time   = %g seconds\n", 
          perf.cum_time - past_perf.cum_time); 
        fprintf(fout, "==    Idle time = %g seconds\n", 
          perf.cum_idle_time - past_perf.cum_idle_time); 
        fprintf(fout, "==    Proc time = %g seconds\n", 
          perf.cum_proc_time - past_perf.cum_proc_time);
        fprintf(fout, "== - - - - - CUMMULATIVE SUMMARY - - - - - -\n");  
        fprintf(fout, "== Cum Hashes = %g\n", perf.cum_hashes);        
        fprintf(fout, "== Cum Energy = %g joules\n", perf.cum_energy);  
        fprintf(fout, "== Cum Time   = %g seconds\n", perf.cum_time); 
        fprintf(fout, "==    Idle time = %g seconds\n", perf.cum_idle_time); 
        fprintf(fout, "==    Proc time = %g seconds\n", perf.cum_proc_time);                
        fprintf(fout, "== Cum Performance\n");         
        fprintf(fout, "==    MH/j      = %g\n", perf.mhash_per_j); 
        fprintf(fout, "==    MH/s      = %g\n", perf.mhash_per_s);
        fprintf(fout, "============= END BLOCK RECORD =============\n");      
        
        fflush(fout);
        
        past_perf = perf;
      
      }
      else if (perf.flag == 1) {
        printf("\n\n"); 
        printf("=================== ABORT ==================\n");      
        printf("Monitor:cum blocks    = %g\n", perf.cum_blocks); 
        
        fprintf(fout, "\n\n");
        fprintf(fout, "=================== ABORT ==================\n"); 
        fprintf(fout, "== Cum Hashes = %g\n", perf.cum_hashes); 
        fprintf(fout, "============================================\n");         
        fflush(fout);        
                       	
      }
      else {
        printf("\n\n"); 
        printf("================= TIMEOUT ==================\n");      
        printf("Monitor:cum blocks    = %g\n", perf.cum_blocks);
        
        fprintf(fout, "\n\n");
        fprintf(fout, "================= TIMEOUT ==================\n"); 
        fprintf(fout, "== Cum Hashes = %g\n", perf.cum_hashes); 
        fprintf(fout, "============================================\n");         
        fflush(fout);          
      }
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
      c_core_log.receive(&log_msg, sizeof(log_msg));

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
      c_wallet_log.receive(&log_msg, sizeof(log_msg));

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
