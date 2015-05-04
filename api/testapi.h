/****************************************************************************
*  Title: testapi.h
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Defines the test bench APIs.
****************************************************************************/

#ifndef _TESTAPI_H_
#define _TESTAPI_H_

#define MAX_WALLET_LOG_MSG_SIZE    256
#define MAX_CORE_LOG_MSG_SIZE     1024

typedef enum
{
  CREATE_BLOCK,
  CREATE_TRANSACTION,
  SPEND_BITCOIN,
  REQUEST_BALANCE

} EventType;

typedef struct
{
  int timestamp;
  EventType type;
  Block block;
  Transaction transaction;

} Event;

#endif /*_TESTAPI_H_ */
