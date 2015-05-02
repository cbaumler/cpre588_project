/****************************************************************************
*  Title: sw_miner.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Top-level mining software behavior (testbench version, just
*     to help verify the hardare miner before release to the larger project)
****************************************************************************/

#include <sim.sh>
#include <stdlib.h>
#include <stdio.h>

import "c_double_handshake";

typedef struct {
	unsigned int version;
	unsigned char prev_hash[32];
	unsigned char merkle_root[32];
	unsigned int time;
	unsigned int nbits;
  unsigned int nonce;
} Block_Header;


typedef struct {
  unsigned int nonce;
	bool status;
} Return_Nonce;


behavior SW_Miner(i_sender   c_abort,
                  i_sender   c_blk_hdr, 
                  i_receiver c_nonce) {

  Block_Header blk;
  Return_Nonce nonce;
  
  // DJK: Temp for testing:
  unsigned char temp_merkle_char;
  int i;

  void main(void) {
  	while (true) {
  	  printf("\nsw_miner: sending block header");  		
  	  c_blk_hdr.send(&blk, sizeof(blk));
  	  c_nonce.receive(&nonce, sizeof(nonce));
  	  printf("\nsw_miner: rx nonce: %u, valid: %d", nonce.nonce, nonce.status);
  	  // DJK: Temp: Modify block header to generate different hash results
  	  temp_merkle_char = blk.merkle_root[0];
  	  for (i = 0; i < 32; i++) {
  	    if (i == 31) {
  	    	blk.merkle_root[31] = temp_merkle_char;
  	    }
  	    else {
  	      blk.merkle_root[i] = blk.merkle_root[i+1];
  	    }
  	  } // end temp loop
  	}
  } // end SW_Miner.main()

}; // end SW_Miner()
