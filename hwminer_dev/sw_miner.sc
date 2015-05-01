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

behavior SW_Miner(i_sender   c_abort,
                  i_sender   c_blk_hdr, 
                  i_receiver c_nonce) {

  int blk;
  int nonce;
  
  void main(void) {
  	while (true) {
  	  printf("\nsw_miner: sending block header");  		
  	  c_blk_hdr.send(&blk, sizeof(blk));
  	  printf("\nsw_miner: block header sent");
  	  c_nonce.receive(&nonce, sizeof(nonce));
  	  printf("\nsw_miner: nonce received");
  	}
  } // end SW_Miner.main()

}; // end SW_Miner()
