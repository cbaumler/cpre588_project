/****************************************************************************
*  Title: tlmbus.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: TLM bus for communication
****************************************************************************/

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define NUM_CHANNELS      4
#define BUFFER_SIZE    1024

typedef enum
{
  UNDEFINED = 0,
  C1        = 1,
  C2        = 2,
  C3        = 3,
  C4        = 4
} BusAddr;


typedef struct
{
  char buffer[BUFFER_SIZE];
  BusAddr addr;

} ChannelBuffer;

interface ISignal
{
  void set(unsigned int val, BusAddr addr);
  void waituntil(unsigned int val, BusAddr addr);
};

behavior Signal() implements ISignal
{
  signal unsigned int sig1;
  signal unsigned int sig2;
  signal unsigned int sig3;
  signal unsigned int sig4;

  void set(unsigned int val, BusAddr addr)
  {
    switch (addr)
    {
      case C1:
      {
        sig1 = val;
        break;
      }
      case C2:
      {
        sig2 = val;
        break;
      }
      case C3:
      {
        sig3 = val;
        break;
      }
      case C4:
      {
        sig4 = val;
        break;
      }
      default:
      {
        printf("tlmbus failed\n");
        exit(1);
      }
    }
  }

  void waituntil(unsigned int val, BusAddr addr)
  {
    switch (addr)
    {
      case C1:
      {
        while (sig1 != val)
        {
          wait sig1;
        }
        break;
      }
      case C2:
      {
        while (sig2 != val)
        {
          wait sig2;
        }
        break;
      }
      case C3:
      {
        while (sig3 != val)
        {
          wait sig3;
        }
        break;
      }
      case C4:
      {
        while (sig4 != val)
        {
          wait sig4;
        }
        break;
      }
      default:
      {
        printf("tlmbus failed\n");
        exit(1);
      }
    }
  }

  void main(void)
  {
  }
};

interface IProtocolMaster
{
  void masterWrite(BusAddr a, void *data, int size);
  void masterRead(BusAddr a, void *data, int size);
};

interface IProtocolSlave
{
  void slaveWrite(BusAddr a, void *data, int size);
  void slaveRead(BusAddr a, void *data, int size);
};

channel TLMBus() implements IProtocolMaster, IProtocolSlave
{
  Signal ready, ack;
  ChannelBuffer chan[NUM_CHANNELS+1];

  void masterWrite(BusAddr a, void *data, int size)
  {
    chan[a].addr = a;
    memcpy(chan[a].buffer, data, size);
    waitfor(15);
    ready.set(1, a);
    ack.waituntil(1, a);
    waitfor(25);
    ready.set(0, a);
    ack.waituntil(0, a);
    chan[a].addr = UNDEFINED;
  }

  void masterRead(BusAddr a, void *data, int size)
  {
    chan[a].addr = a;
    waitfor(15);
    ready.set(1, a);
    ack.waituntil(1, a);
    memcpy(data, chan[a].buffer, size);
    waitfor(25);
    ready.set(0, a);
    ack.waituntil(0, a);
    chan[a].addr = UNDEFINED;
  }

  void slaveWrite(BusAddr a, void *data, int size)
  {
    while (a != chan[a].addr)
    {
      ready.waituntil(1, a);
    }
    memcpy(chan[a].buffer, data, size);
    waitfor(20);
    ack.set(1, a);
    ready.waituntil(0, a);
    waitfor(15);
    ack.set(0, a);
  }

  void slaveRead(BusAddr a, void *data, int size)
  {
    while (a != chan[a].addr)
    {
      ready.waituntil(1, a);
    }
    memcpy(data, chan[a].buffer, size);
    waitfor(20);
    ack.set(1, a);
    ready.waituntil(0, a);
    waitfor(15);
    ack.set(0, a);
  }
};
