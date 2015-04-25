/****************************************************************************
*  Title: hwconfig.h
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Defines the hardware configuration parameters
****************************************************************************/

#define MAX_NAME_LENGTH    256

#define NUM_HW_PARAMETERS   16

// This struct defines the hardware input parameters.
// Note that these must be of type int to be parsed correctly.
typedef struct
{
  // PE Performance
  int clock;
  int pipeline_depth;
  int parallel;

  // Composite Operations
  int bif;
  int call;

  // SHA-256 Instruction Set
  int isum;
  int imul;
  int idiv;
  int shft;
  int rot;
  int band;
  int bor;
  int bnot;
  int bxor;
  int comp;

  // Power Consumption
  int pwr;

} HWConfig;
