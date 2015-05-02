/****************************************************************************
*  Title: hwconfig.h
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Defines the hardware configuration parameters
****************************************************************************/

#ifndef __HWCONFIG__
#define __HWCONFIG__


#define MAX_NAME_LENGTH    256

#define NUM_HW_PARAMETERS   19


// Array indices

// PE Performance
#define ix_clock               0
#define ix_pipeline_depth      1
#define ix_parallel            2
  
// Memory Performance
#define ix_mread               3
#define ix_mwrite              4
  
// Composite Operations
#define ix_bif                 5
#define ix_call                6

// SHA-256 Instruction Set
#define ix_isum                7
#define ix_imul                8
#define ix_idiv                9
#define ix_shft               10
#define ix_rot                11
#define ix_band               12
#define ix_bor                13
#define ix_bnot               14
#define ix_bxor               15
#define ix_comp               16
  
// Hash algorithm time limit (seconds)
#define ix_timeout            17

// Power Consumption
#define ix_pwr                18




// This struct defines the hardware input parameters.
// Note that these must be of type int to be parsed correctly.
/*typedef struct
{
  // PE Performance
  int clock;
  int pipeline_depth;
  int parallel;
  
  // Memory Performance
  int mread;
  int mwrite;
  
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
  
  // Hash algorithm time limit (seconds)
  int timeout;

  // Power Consumption
  int pwr;

} PE_Profile;*/

#endif
