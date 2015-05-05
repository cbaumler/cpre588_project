/****************************************************************************
*  Title: stimulus.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Sends transactions and parametric data to the design
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
# include "../api/coreapi.h"     // Defines the core Bitcoin interfaces
#include "../config/hwconfig.h"  // Defines hardware config parameters
#include "../api/testapi.h"      // Defines test interfaces

#define FGETS_MAX   100

import "c_double_handshake";	// import the standard channel
import "rpcclient";

behavior Stimulus(i_sender c_p2p_request, i_receiver c_p2p_response,
  i_sender c_profile, i_sender c_wallet_cmd,
  out unsigned int mining_difficulty_output)
{
  RPCClient client(c_p2p_request, c_p2p_response);

  void main (void)
  {
    FILE *fhwconfig;
    FILE *ftransactions;
    FILE *fevents;
    char name[MAX_NAME_LENGTH];
    char pe_type[MAX_NAME_LENGTH];    
    int value, idx, err;
    HWConfig hwconfig;
    int *p_hwconfig;
    Event events[50];
    Transaction transactions[MAX_TRANSACTIONS];
    int event_write_index = 0;
    int tx_write_index = 0;
    int tx_read_index = 0;
    int txid, utxoid, addr, amt, timestamp, num_tx;
    unsigned int mining_difficulty;
    int simulation_time;
    char line[FGETS_MAX];
    char temp[FGETS_MAX];
    char event_str[64];

    p_hwconfig = (int*)(&(hwconfig.clock));

    fhwconfig = fopen("../config/hardware.cfg", "r");
    ftransactions = fopen("../config/transactions.cfg", "r");
    fevents = fopen("../config/events.cfg", "r");

    if (fhwconfig != NULL)
    {
      printf("Hardware Configuration:\n");
      if (fscanf(fhwconfig, "%s", &pe_type) == EOF)
        {
          fprintf(stderr, "No PE Type name in hardware.cfg\n");
          exit(1);
        }
      printf("%s\n", pe_type);
      for (idx = 0; idx < NUM_HW_PARAMETERS; idx++)
      {
        if (fscanf(fhwconfig, "%s %ul", &name, &value) == EOF)
        {
          fprintf(stderr, "Too few parameters in hardware.cfg\n");
          exit(1);
        }
        p_hwconfig[idx] = value;
        printf("%s=%ul\n", name, value);
      }
    }
    else
    {
      fprintf(stderr, "Failed to read hardware.cfg\n");
      exit(1);
    }

    if (ftransactions != NULL)
    {
      // Read the configuration file header
      fgets(line, FGETS_MAX, ftransactions);

      // Read the transactions
      while (fscanf(ftransactions, "%d %d %d %d", &txid, &utxoid, &addr, &amt) != EOF)
      {
        // Add the transactions to an array
        transactions[tx_write_index].txid = txid;
        transactions[tx_write_index].input.txid = utxoid;
        transactions[tx_write_index].input.vout = 0;
        transactions[tx_write_index].output.txid = txid;
        transactions[tx_write_index].output.vout = 0;
        transactions[tx_write_index].address = addr;
        transactions[tx_write_index].amount = amt;

        // Use random numbers for these, because they won't matter for the
        // sake of our simulation
        transactions[tx_write_index].private_key = rand();
        transactions[tx_write_index].raw_transaction = rand();
        transactions[tx_write_index].signed_transaction = rand();

        // Increment the array write index
        tx_write_index++;
      }
    }
    else
    {
      fprintf(stderr, "Failed to read transactions.cfg\n");
      exit(1);
    }

    if (fevents != NULL)
    {
      // Read the simulation time
      fgets(line, FGETS_MAX, fevents);
      sscanf(line, "%s %d", temp, &simulation_time);
      printf("Simulation Time: %d\n", simulation_time);

      // Read the mining difficulty
      fgets(line, FGETS_MAX, fevents);
      sscanf(line, "%s %d", temp, &mining_difficulty);
      mining_difficulty_output = mining_difficulty;
      printf("Mining difficulty: %d\n", mining_difficulty);

      // Read the blank line
      fgets(line, FGETS_MAX, fevents);

      // Read the configuration file header
      fgets(line, FGETS_MAX, fevents);

      // Read the events
      while (fscanf(fevents, "%d %s %d", &timestamp, event_str, &num_tx) != EOF)
      {
        if (strcmp(event_str, "CREATE_BLOCK") == 0)
        {
          // Create a block event for submitting a block to our node.
          // This simulates getting a block from the Peer-to-Peer network.
          events[event_write_index].type = CREATE_BLOCK;
          events[event_write_index].timestamp = timestamp;
          events[event_write_index].block.header.version = BLOCK_VERSION;
          events[event_write_index].block.header.current_time = (int)time(0);
          events[event_write_index].block.header.nbits = mining_difficulty;

          // To avoid the complexity of having to determine these, use
          // a random number. This shouldn't matter for our simulations.
          events[event_write_index].block.header.merkle_root = (unsigned int)rand();
          for (idx = 0; idx < NUM_HASH_BYTES; idx++)
          {
            events[event_write_index].block.header.prev_hash[idx] = (unsigned char)(rand()%255);
            events[event_write_index].block.hash[idx] = (unsigned char)(rand()%255);
          }

          // Add transactions to the block
          events[event_write_index].block.n_transactions = num_tx;
          for (idx = 0; idx < num_tx; idx++)
          {
            if (tx_read_index < tx_write_index)
            {
              memcpy(&(events[event_write_index].block.transactions[idx]),
                     &(transactions[tx_read_index]), sizeof(Transaction));
              tx_read_index++;
            }
            else
            {
              fprintf(stderr, "Not enough transactions in transactions.cfg\n");
              exit(1);
            }
          }

          // Increment the array write index
          event_write_index++;
        }
        else if (strcmp(event_str, "CREATE_TRANSACTION") == 0)
        {
          for (idx = 0; idx < num_tx; idx++)
          {
            // Create a transaction event to send a transaction to our node
            events[event_write_index].type = CREATE_TRANSACTION;
            events[event_write_index].timestamp = timestamp;
            memcpy(&(events[event_write_index].transaction),
              &(transactions[tx_read_index]), sizeof(Transaction));
            tx_read_index++;
            event_write_index++;
          }
        }
        else if (strcmp(event_str, "SPEND_BITCOIN") == 0)
        {
          for (idx = 0; idx < num_tx; idx++)
          {
            // Create an event to spend Bitcoin
            events[event_write_index].type = SPEND_BITCOIN;
            events[event_write_index].timestamp = timestamp;
            event_write_index++;
          }
        }
        else if (strcmp(event_str, "REQUEST_BALANCE") == 0)
        {
          for (idx = 0; idx < num_tx; idx++)
          {
            // Create an event to request the user's wallet balance
            events[event_write_index].type = REQUEST_BALANCE;
            events[event_write_index].timestamp = timestamp;
            event_write_index++;
          }
        }
        else
        {
          fprintf(stderr, "Invalid action in events.cfg: %s\n", event_str);
          exit(1);
        }
      }
    }
    else
    {
      fprintf(stderr, "Failed to read events.cfg\n");
      exit(1);
    }

    // Send the hardware configuration data to the hardware miner
    c_profile.send(&hwconfig, sizeof(hwconfig));

    // Inject the events from the event configuration file
    for (idx = 0; idx < event_write_index; idx++)
    {
      // Wait until the correct time to inject the event
      waitfor(events[idx].timestamp);

      if (events[idx].type == CREATE_BLOCK)
      {
        printf("Stimulus: Injecting block from P2P Network\n");
        err = client.submitblock(&(events[idx].block));
        if (err == -1)
        {
          fprintf(stderr, "stimulus: submitblock failed\n");
          exit (1);
        }
      }
      else if (events[idx].type == CREATE_TRANSACTION)
      {
        printf("Stimulus: Injecting transaction from P2P Network\n");
        err = client.dev_sendrawtransaction(events[idx].transaction);
        if (err == -1)
        {
          fprintf(stderr, "stimulus: dev_sendrawtransaction failed\n");
          exit (1);
        }
      }
      else if ((events[idx].type == SPEND_BITCOIN) ||
              (events[idx].type == REQUEST_BALANCE))
      {
        c_wallet_cmd.send(&(events[idx].type), sizeof(EventType));
      }
    }

    // Terminate the simulation after the desired amount of time
    waitfor(simulation_time);
    //printf("Simulation Complete\n");
    //exit (0);
  }
};
