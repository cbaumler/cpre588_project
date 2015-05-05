/****************************************************************************
*  Title: hwconfig.h
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Defines the hardware configuration parameters
****************************************************************************/

#define MAX_NAME_LENGTH    256

#define NUM_HW_PARAMETERS   17

// This struct defines the hardware input parameters.
// Note that these must be of type int to be parsed correctly.
typedef struct
{
  // PE Performance
  unsigned long clock;
  
  // Memory Performance
  unsigned long mread;
  unsigned long mwrite;  

  // Composite Operations
  unsigned long call;

  // SHA-256 Instruction Set
  unsigned long isum;
  unsigned long imul;
  unsigned long idiv;
  unsigned long shft;
  unsigned long rot;
  unsigned long band;
  unsigned long bor;
  unsigned long bnot;
  unsigned long bxor;
  unsigned long comp;

  // Power Consumption
  unsigned long timeout;
  
  // Power Consumption
  unsigned long pwr_static;
  unsigned long pwr_dynamic;

} HWConfig;
