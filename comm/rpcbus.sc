/****************************************************************************
*  Title: rpcbus.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: TLM bus for remote procedure calls
****************************************************************************/

typedef enum {UNDEFINED, C1, C2} BusAddr;

interface ISignal
{
  void set(unsigned int val);
  void waituntil(unsigned int val);
};
