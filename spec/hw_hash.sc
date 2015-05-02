//-------------------------------------------------------------------------
// Behavior: HW_Hash
// Author: Team 4
// Date: May 6, 2015
// Description:
// Inputs:
// Outputs:
//-------------------------------------------------------------------------

#include <sim.sh>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#include "../api/coreapi.h"

import "c_double_handshake";	// import the standard channel


//------------------------------------------------------------------------
// Instrumentation Constants
//   (to be replaced with file-based data eventually, to simplify swithing
//    between different PE types)
//
#define t_clock                 5
#define t_pipeline_depth        4
#define t_parallel_paths        4
#define t_mread                 1
#define t_mwrite                1
#define t_bif                   30
#define t_call                  10
#define t_isum                  5
#define t_imul                  15
#define t_idiv                  20
#define t_shft                  2
#define t_rot                   2
#define t_band                  5
#define t_bor                   5
#define t_bnot                  5
#define t_bxor                  5
#define t_comp                  3
#define t_timeout               10
#define t_pwr                   0

typedef struct {
	unsigned char midstate[256]; // Warning: Picked an arbitrary size
  unsigned char data[256];
	unsigned char hash[256];
	unsigned char target[256];
} Work;


typedef struct {
  unsigned int nonce;
	bool status;
} Return_Nonce;


typedef struct {

	// Time statistics
  long long time_total;     // Time since start of simulation
  long long time_blk_start; // Time at start of block processing
  long long time_blk_stop;  // Time at end of current block processing
  long long time_blk;       // Time for processing current block

  // Hash count statistics
  long hash_total;          // Total hash count since start of simulation
  long hash_blk;            // Current hash count accumulator for current block

  // Power statistics
  long power_total;         // Total power since start of simulation
  long power_blk;           // Current power accumulator for current block

} Statistics;



behavior Process_Block_Header(i_receiver  c_blk_hdr,
                              i_sender    c_nonce,
                              i_sender    c_perf,
                              event       e_tstart,
                              event       e_tstop) {

  // Behavior-global data
	BlockHeader g_blk_hdr;
	Return_Nonce g_nonce;
	Statistics g_stats;


	//***********************************//
	//** INSTRUMENTED CODE STARTS HERE **//
	//***********************************//


	//-----------------------------------------------------------------------
	// Constant: Process_Block_Header.sha256_init_state
	// Description:
	//-----------------------------------------------------------------------
  const unsigned int sha256_init_state[8] = {
  	0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
  	0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
  };


	//-----------------------------------------------------------------------
	// Function: General hashing utilities
	// Description:
	// Inputs:
	// Outputs:
	//-----------------------------------------------------------------------

  typedef unsigned int u32;
  typedef unsigned char u8;

  u32 ror32(u32 word, unsigned int shift) {
  	// Instrumentation block
  	waitfor(t_shft*(32-shift+1) + t_bor);
  	return (word >> shift) | (word << (32 - shift));
  }


  u32 Ch(u32 x, u32 y, u32 z) {
  	// Instrumentation block
  	waitfor(t_bxor*2 + t_band);
  	return z ^ (x & (y ^ z));
  }


  u32 Maj(u32 x, u32 y, u32 z) {
  	// Instrumentation block
  	waitfor(t_band*2 + t_bor*2);
  	return (x & y) | (z & (x | y));
  }

  // Instrumentation:	waitfor(t_rot*37 + t_bxor*2);
  #define e0(x) (ror32(x, 2) ^ ror32(x,13) ^ ror32(x,22))

  // Instrumentation:	waitfor(t_rot*42 + t_bxor*2);
  #define e1(x) (ror32(x, 6) ^ ror32(x,11) ^ ror32(x,25))

  // Instrumentation:	waitfor(t_rot*25 + t_shft*3 + t_bxor*2);
  #define s0(x) (ror32(x, 7) ^ ror32(x,18) ^ (x >> 3))

  // Instrumentation:	waitfor(t_rot*36 + t_shft*10 + t_bxor*2);
  #define s1(x) (ror32(x,17) ^ ror32(x,19) ^ (x >> 10))

  // Instrumentation:	waitfor(t_shft*16 + t_band +t_bor*2);
  #define	bswap_16(value)  \
 	  ((((value) & 0xff) << 8) | ((value) >> 8))

 	// Instrumentation:	waitfor(t_band + t_shft*32);
  #define	bswap_32(value)	\
   	(((unsigned int)bswap_16((unsigned short)((value) & 0xffff)) << 16) | \
   	(unsigned int)bswap_16((unsigned short)((value) >> 16)))


  void LOAD_OP(int I, u32 *W, const u8 *input) {
  	// Instrumentation block
  	// waitfor(t_mread + t_mwrite);  	  *** Skip memory read/write ops for now
  	W[I] = ( ((u32*)(input))[I] );
  }


  void BLEND_OP(int I, u32 *W) {
  	// Instrumentation block
  	waitfor(t_isum*7);
  	W[I] = s1(W[I-2]) + W[I-7] + s0(W[I-15]) + W[I-16];
  }


  unsigned int swab32(unsigned int v) {
  	return bswap_32(v);
  }


	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.sha256_transform
	// Description:
	// Inputs:
	// Outputs:
	//-----------------------------------------------------------------------
  void sha256_transform(u32 *state,
                        const u8 *input) {

  	u32 a, b, c, d, e, f, g, h, t1, t2;
  	u32 W[64];
  	int i;

  	/* load the input */
  	// Instrumentation block
  	waitfor(t_comp*16 + t_isum*16);
  	for (i = 0; i < 16; i++)
  		LOAD_OP(i, W, input);

  	/* now blend */
  	// Instrumentation block
  	waitfor(t_comp*48 + t_isum*48);
  	for (i = 16; i < 64; i++)
  		BLEND_OP(i, W);

  	/* load the state into our registers */
  	a=state[0];  b=state[1];  c=state[2];  d=state[3];
  	e=state[4];  f=state[5];  g=state[6];  h=state[7];

  	/* now iterate */
  	// Instrumentation block(s)
  	waitfor((t_isum*4)*64);   // For statements of the form "t1 = h + e1(e) + Ch(e,f,g) + 0x428a2f98 + W[ 0];"
  	waitfor((t_isum*3)*64);   // For statements of the form "t2 = e0(a) + Maj(a,b,c);    d+=t1;    h=t1+t2;"
  	t1 = h + e1(e) + Ch(e,f,g) + 0x428a2f98 + W[ 0];
  	t2 = e0(a) + Maj(a,b,c);    d+=t1;    h=t1+t2;
  	t1 = g + e1(d) + Ch(d,e,f) + 0x71374491 + W[ 1];
  	t2 = e0(h) + Maj(h,a,b);    c+=t1;    g=t1+t2;
  	t1 = f + e1(c) + Ch(c,d,e) + 0xb5c0fbcf + W[ 2];
  	t2 = e0(g) + Maj(g,h,a);    b+=t1;    f=t1+t2;
  	t1 = e + e1(b) + Ch(b,c,d) + 0xe9b5dba5 + W[ 3];
  	t2 = e0(f) + Maj(f,g,h);    a+=t1;    e=t1+t2;
  	t1 = d + e1(a) + Ch(a,b,c) + 0x3956c25b + W[ 4];
  	t2 = e0(e) + Maj(e,f,g);    h+=t1;    d=t1+t2;
  	t1 = c + e1(h) + Ch(h,a,b) + 0x59f111f1 + W[ 5];
  	t2 = e0(d) + Maj(d,e,f);    g+=t1;    c=t1+t2;
  	t1 = b + e1(g) + Ch(g,h,a) + 0x923f82a4 + W[ 6];
  	t2 = e0(c) + Maj(c,d,e);    f+=t1;    b=t1+t2;
  	t1 = a + e1(f) + Ch(f,g,h) + 0xab1c5ed5 + W[ 7];
  	t2 = e0(b) + Maj(b,c,d);    e+=t1;    a=t1+t2;

  	t1 = h + e1(e) + Ch(e,f,g) + 0xd807aa98 + W[ 8];
  	t2 = e0(a) + Maj(a,b,c);    d+=t1;    h=t1+t2;
  	t1 = g + e1(d) + Ch(d,e,f) + 0x12835b01 + W[ 9];
  	t2 = e0(h) + Maj(h,a,b);    c+=t1;    g=t1+t2;
  	t1 = f + e1(c) + Ch(c,d,e) + 0x243185be + W[10];
  	t2 = e0(g) + Maj(g,h,a);    b+=t1;    f=t1+t2;
  	t1 = e + e1(b) + Ch(b,c,d) + 0x550c7dc3 + W[11];
  	t2 = e0(f) + Maj(f,g,h);    a+=t1;    e=t1+t2;
  	t1 = d + e1(a) + Ch(a,b,c) + 0x72be5d74 + W[12];
  	t2 = e0(e) + Maj(e,f,g);    h+=t1;    d=t1+t2;
  	t1 = c + e1(h) + Ch(h,a,b) + 0x80deb1fe + W[13];
  	t2 = e0(d) + Maj(d,e,f);    g+=t1;    c=t1+t2;
  	t1 = b + e1(g) + Ch(g,h,a) + 0x9bdc06a7 + W[14];
  	t2 = e0(c) + Maj(c,d,e);    f+=t1;    b=t1+t2;
  	t1 = a + e1(f) + Ch(f,g,h) + 0xc19bf174 + W[15];
  	t2 = e0(b) + Maj(b,c,d);    e+=t1;    a=t1+t2;

  	t1 = h + e1(e) + Ch(e,f,g) + 0xe49b69c1 + W[16];
  	t2 = e0(a) + Maj(a,b,c);    d+=t1;    h=t1+t2;
  	t1 = g + e1(d) + Ch(d,e,f) + 0xefbe4786 + W[17];
  	t2 = e0(h) + Maj(h,a,b);    c+=t1;    g=t1+t2;
  	t1 = f + e1(c) + Ch(c,d,e) + 0x0fc19dc6 + W[18];
  	t2 = e0(g) + Maj(g,h,a);    b+=t1;    f=t1+t2;
  	t1 = e + e1(b) + Ch(b,c,d) + 0x240ca1cc + W[19];
  	t2 = e0(f) + Maj(f,g,h);    a+=t1;    e=t1+t2;
  	t1 = d + e1(a) + Ch(a,b,c) + 0x2de92c6f + W[20];
  	t2 = e0(e) + Maj(e,f,g);    h+=t1;    d=t1+t2;
  	t1 = c + e1(h) + Ch(h,a,b) + 0x4a7484aa + W[21];
  	t2 = e0(d) + Maj(d,e,f);    g+=t1;    c=t1+t2;
  	t1 = b + e1(g) + Ch(g,h,a) + 0x5cb0a9dc + W[22];
  	t2 = e0(c) + Maj(c,d,e);    f+=t1;    b=t1+t2;
  	t1 = a + e1(f) + Ch(f,g,h) + 0x76f988da + W[23];
  	t2 = e0(b) + Maj(b,c,d);    e+=t1;    a=t1+t2;

  	t1 = h + e1(e) + Ch(e,f,g) + 0x983e5152 + W[24];
  	t2 = e0(a) + Maj(a,b,c);    d+=t1;    h=t1+t2;
  	t1 = g + e1(d) + Ch(d,e,f) + 0xa831c66d + W[25];
  	t2 = e0(h) + Maj(h,a,b);    c+=t1;    g=t1+t2;
  	t1 = f + e1(c) + Ch(c,d,e) + 0xb00327c8 + W[26];
  	t2 = e0(g) + Maj(g,h,a);    b+=t1;    f=t1+t2;
  	t1 = e + e1(b) + Ch(b,c,d) + 0xbf597fc7 + W[27];
  	t2 = e0(f) + Maj(f,g,h);    a+=t1;    e=t1+t2;
  	t1 = d + e1(a) + Ch(a,b,c) + 0xc6e00bf3 + W[28];
  	t2 = e0(e) + Maj(e,f,g);    h+=t1;    d=t1+t2;
  	t1 = c + e1(h) + Ch(h,a,b) + 0xd5a79147 + W[29];
  	t2 = e0(d) + Maj(d,e,f);    g+=t1;    c=t1+t2;
  	t1 = b + e1(g) + Ch(g,h,a) + 0x06ca6351 + W[30];
  	t2 = e0(c) + Maj(c,d,e);    f+=t1;    b=t1+t2;
  	t1 = a + e1(f) + Ch(f,g,h) + 0x14292967 + W[31];
  	t2 = e0(b) + Maj(b,c,d);    e+=t1;    a=t1+t2;

  	t1 = h + e1(e) + Ch(e,f,g) + 0x27b70a85 + W[32];
  	t2 = e0(a) + Maj(a,b,c);    d+=t1;    h=t1+t2;
  	t1 = g + e1(d) + Ch(d,e,f) + 0x2e1b2138 + W[33];
  	t2 = e0(h) + Maj(h,a,b);    c+=t1;    g=t1+t2;
  	t1 = f + e1(c) + Ch(c,d,e) + 0x4d2c6dfc + W[34];
  	t2 = e0(g) + Maj(g,h,a);    b+=t1;    f=t1+t2;
  	t1 = e + e1(b) + Ch(b,c,d) + 0x53380d13 + W[35];
  	t2 = e0(f) + Maj(f,g,h);    a+=t1;    e=t1+t2;
  	t1 = d + e1(a) + Ch(a,b,c) + 0x650a7354 + W[36];
  	t2 = e0(e) + Maj(e,f,g);    h+=t1;    d=t1+t2;
  	t1 = c + e1(h) + Ch(h,a,b) + 0x766a0abb + W[37];
  	t2 = e0(d) + Maj(d,e,f);    g+=t1;    c=t1+t2;
  	t1 = b + e1(g) + Ch(g,h,a) + 0x81c2c92e + W[38];
  	t2 = e0(c) + Maj(c,d,e);    f+=t1;    b=t1+t2;
  	t1 = a + e1(f) + Ch(f,g,h) + 0x92722c85 + W[39];
  	t2 = e0(b) + Maj(b,c,d);    e+=t1;    a=t1+t2;

  	t1 = h + e1(e) + Ch(e,f,g) + 0xa2bfe8a1 + W[40];
  	t2 = e0(a) + Maj(a,b,c);    d+=t1;    h=t1+t2;
  	t1 = g + e1(d) + Ch(d,e,f) + 0xa81a664b + W[41];
  	t2 = e0(h) + Maj(h,a,b);    c+=t1;    g=t1+t2;
  	t1 = f + e1(c) + Ch(c,d,e) + 0xc24b8b70 + W[42];
  	t2 = e0(g) + Maj(g,h,a);    b+=t1;    f=t1+t2;
  	t1 = e + e1(b) + Ch(b,c,d) + 0xc76c51a3 + W[43];
  	t2 = e0(f) + Maj(f,g,h);    a+=t1;    e=t1+t2;
  	t1 = d + e1(a) + Ch(a,b,c) + 0xd192e819 + W[44];
  	t2 = e0(e) + Maj(e,f,g);    h+=t1;    d=t1+t2;
  	t1 = c + e1(h) + Ch(h,a,b) + 0xd6990624 + W[45];
  	t2 = e0(d) + Maj(d,e,f);    g+=t1;    c=t1+t2;
  	t1 = b + e1(g) + Ch(g,h,a) + 0xf40e3585 + W[46];
  	t2 = e0(c) + Maj(c,d,e);    f+=t1;    b=t1+t2;
  	t1 = a + e1(f) + Ch(f,g,h) + 0x106aa070 + W[47];
  	t2 = e0(b) + Maj(b,c,d);    e+=t1;    a=t1+t2;

  	t1 = h + e1(e) + Ch(e,f,g) + 0x19a4c116 + W[48];
  	t2 = e0(a) + Maj(a,b,c);    d+=t1;    h=t1+t2;
  	t1 = g + e1(d) + Ch(d,e,f) + 0x1e376c08 + W[49];
  	t2 = e0(h) + Maj(h,a,b);    c+=t1;    g=t1+t2;
  	t1 = f + e1(c) + Ch(c,d,e) + 0x2748774c + W[50];
  	t2 = e0(g) + Maj(g,h,a);    b+=t1;    f=t1+t2;
  	t1 = e + e1(b) + Ch(b,c,d) + 0x34b0bcb5 + W[51];
  	t2 = e0(f) + Maj(f,g,h);    a+=t1;    e=t1+t2;
  	t1 = d + e1(a) + Ch(a,b,c) + 0x391c0cb3 + W[52];
  	t2 = e0(e) + Maj(e,f,g);    h+=t1;    d=t1+t2;
  	t1 = c + e1(h) + Ch(h,a,b) + 0x4ed8aa4a + W[53];
  	t2 = e0(d) + Maj(d,e,f);    g+=t1;    c=t1+t2;
  	t1 = b + e1(g) + Ch(g,h,a) + 0x5b9cca4f + W[54];
  	t2 = e0(c) + Maj(c,d,e);    f+=t1;    b=t1+t2;
  	t1 = a + e1(f) + Ch(f,g,h) + 0x682e6ff3 + W[55];
  	t2 = e0(b) + Maj(b,c,d);    e+=t1;    a=t1+t2;

  	t1 = h + e1(e) + Ch(e,f,g) + 0x748f82ee + W[56];
  	t2 = e0(a) + Maj(a,b,c);    d+=t1;    h=t1+t2;
  	t1 = g + e1(d) + Ch(d,e,f) + 0x78a5636f + W[57];
  	t2 = e0(h) + Maj(h,a,b);    c+=t1;    g=t1+t2;
  	t1 = f + e1(c) + Ch(c,d,e) + 0x84c87814 + W[58];
  	t2 = e0(g) + Maj(g,h,a);    b+=t1;    f=t1+t2;
  	t1 = e + e1(b) + Ch(b,c,d) + 0x8cc70208 + W[59];
  	t2 = e0(f) + Maj(f,g,h);    a+=t1;    e=t1+t2;
  	t1 = d + e1(a) + Ch(a,b,c) + 0x90befffa + W[60];
  	t2 = e0(e) + Maj(e,f,g);    h+=t1;    d=t1+t2;
  	t1 = c + e1(h) + Ch(h,a,b) + 0xa4506ceb + W[61];
  	t2 = e0(d) + Maj(d,e,f);    g+=t1;    c=t1+t2;
  	t1 = b + e1(g) + Ch(g,h,a) + 0xbef9a3f7 + W[62];
  	t2 = e0(c) + Maj(c,d,e);    f+=t1;    b=t1+t2;
  	t1 = a + e1(f) + Ch(f,g,h) + 0xc67178f2 + W[63];
  	t2 = e0(b) + Maj(b,c,d);    e+=t1;    a=t1+t2;

  	// Instrumentation block(s)
  	waitfor(t_isum*8);
  	state[0] += a; state[1] += b; state[2] += c; state[3] += d;
  	state[4] += e; state[5] += f; state[6] += g; state[7] += h;

  } // end Process_Block_Header.sha256_transform()



	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.runhash
	// Description:
	// Inputs:
	// Outputs:
	//-----------------------------------------------------------------------
  void runhash(void *state,
               const void *input,
               const void *init) {

    // Initialize the state array (32 bytes) from the init parameter
  	memcpy(state, init, 32);

  	// Run the hash algorithm
  	sha256_transform((u32 *)state, (u8 *)input);

  } // end Process_Block_Header.runhash()



	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.swap256
	// Description:
	// Inputs:
	// Outputs:
	//-----------------------------------------------------------------------
  void swap256(void *dest_p, const void *src_p) {

		unsigned int *dest;
		unsigned int *src;

		dest = (unsigned int *)dest_p;
		src = (unsigned int *)src_p;

  	dest[0] = src[7];
  	dest[1] = src[6];
  	dest[2] = src[5];
  	dest[3] = src[4];
  	dest[4] = src[3];
  	dest[5] = src[2];
  	dest[6] = src[1];
  	dest[7] = src[0];
  }


	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.fulltest
	// Description: Compare hash against target value; return true if hash
	//    is less than the target  value.
	// Inputs:
	//    1. Hash value; array of 32 bytes
	//    2. Target value (threshold); array of 32 bytes
	// Outputs:
	//    1. Boolean
	//-----------------------------------------------------------------------
  bool fulltest(const unsigned char *hash,
                const unsigned char *target) {

		unsigned char hash_swap[32], target_swap[32];
		unsigned int *hash32;
		unsigned int *target32;
		int i;
		bool rc = true;
		unsigned int h32tmp, t32tmp;

		hash32 = (unsigned int *) hash_swap;
		target32 = (unsigned int *) target_swap;

  	swap256(hash_swap, hash);
  	swap256(target_swap, target);

  	// Instrumentation block(s)
  	waitfor(t_comp*8 + t_isum*8);
  	for (i = 0; i < 32/4; i++) {

  		h32tmp = swab32(hash32[i]);
  		t32tmp = target32[i];

  		target32[i] = swab32(target32[i]);	/* for printing */

  	  // Instrumentation block(s)
  	  waitfor(t_comp);
  		if (h32tmp >= t32tmp) {
  			rc = false;
  			break;
  		}
  		else {
  			rc = true;
  			break;
  		}
  	}

  	return rc;

  } // end Process_Block_Header.fulltest()


  void hashmeter(unsigned int hash_time, unsigned long hashes_done) {
		// TODO: compute the hashes per second
	}


	void reset_stats(void) {

    // Reset time statistics
    g_stats.time_total     = 0;
    g_stats.time_blk_start = 0;
    g_stats.time_blk_stop  = 0;
    g_stats.time_blk       = 0;

    // Reset hash count statistics
    g_stats.hash_total = 0;
    g_stats.hash_blk   = 0;

    // Reset power statistics
    g_stats.power_total = 0;
    g_stats.power_blk   = 0;

	} // end reset_stats()


	void update_stats(void) {

    // Update time statistics
    g_stats.time_total     = now();
    g_stats.time_blk_start = 0;
    g_stats.time_blk_stop  = 0;
    g_stats.time_blk       = 0;

    // Update hash count statistics
    g_stats.hash_total++;
    g_stats.hash_blk++;

    // Update power statistics
    g_stats.power_total++;
    g_stats.power_blk++;

	} // end update_stats()


	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.scanhash
	// Description:
	// Inputs:
	// Outputs:
	//-----------------------------------------------------------------------
  bool scanhash(unsigned char *midstate,
                unsigned char *data,
  	            unsigned char *hash,
  	            unsigned char *target,
  	            unsigned int   max_nonce,
  	            unsigned long *hashes_done) {

  	unsigned int *hash32;
  	unsigned int *nonce;
  	unsigned int  n        = 0;
  	unsigned long stat_ctr = 0;

		hash32   = (unsigned int *) hash;
		nonce    = (unsigned int *)(data + 12);

  	while (true) {

  		unsigned char hash1[32];

  	  // Instrumentation block(s)
  	  waitfor(t_isum*2); // Covers n++ and stat_ctr++
  		n++;
  		*nonce = n;

  		runhash(hash1, data, midstate);
  		runhash(hash, hash1, sha256_init_state);

  		stat_ctr++;

  		if (fulltest(hash, target)) {
  			*hashes_done = stat_ctr;
  			printf("\nHW_Hash.scanhash(VALID):nonce = %d, count = %lu, sim time = %6s", n, stat_ctr, time2str(now()));
  			//exit(0);
  			return true;
  		}
  		else if (n >= max_nonce) {
  	    // Instrumentation block(s)
  	    waitfor(t_comp);
  			*hashes_done = stat_ctr;
  			return false;
  		}

  	} // end loop

  } // end Process_Block_Header.scanhash()



	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.hash_it_to_pieces
	// Description:
	// Inputs:
	// Outputs:
	//-----------------------------------------------------------------------
  bool hash_it_to_pieces() {

	  unsigned int max_nonce = 0xffffff;
    unsigned long hashes_done;
	  unsigned int t_start, t_end, diff;
	  bool rc;
	  Work work;  // DJK: Not sure work is needed, except still don't know what
	              //      the "midstate" value is supposed to be
    int i;

    // temp target, set to a small value to allow hash to complete
    // more quickly
    unsigned char test_target[32] = {
      0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
      0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
      0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
      0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0x00, 0x00
    };

// DJK: Randomize "work" for testing

    for (i = 0; i < 256; i++) {
    	work.midstate[i] = (unsigned char) (rand() % 256);
    	work.data[i]     = (unsigned char) (rand() % 256);
    	work.hash[i]     = (unsigned char) (rand() % 256);
    }


    // Initialize hash attempt counter and record start time
	  hashes_done = 0;
	  t_start = (unsigned int)time(0); // Unix time

    // TODO: None of the value of work are initialized to anything right now

	  // Scan nonces for a proof-of-work hash
    rc = scanhash(work.midstate,
                  work.data + 64,
			            work.hash,
			            //work.target,
			            test_target,   // temp data for testing
				          max_nonce,
				          &hashes_done);

	  // record scanhash elapsed time
	  t_end = (int)time(0);  // DJK: Time will be recorded in the statistics, using
	                         // sim time; real time won't give us the data we need
	  diff = t_end - t_start;

	  // Compute a hash rate
	  hashmeter(diff, hashes_done);

	  // Report results
	  return rc;

	} // end Process_Block_Header.hash_it_to_pieces()




	//**********************************//
	//** INSTRUMENTED CODE STOPS HERE **//
	//**********************************//


	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.main
	// Description:
	// Inputs:
	// Outputs:
	//-----------------------------------------------------------------------
	void main(void) {

		while (true) {

			reset_stats();

      printf("\nHW_Hash:c_blk_hdr.receive()");

      c_blk_hdr.receive(&g_blk_hdr, sizeof(g_blk_hdr));
      printf("\nHW_Hash:blk_hdr received");

      // Run the hash search function, using the watchdog timer to
      // prevent the search from running endlessley
      notify(e_tstart); // Start watchdog
      g_nonce.status = hash_it_to_pieces();
      notify(e_tstop);  // Stop watchdog

      // Set the final nonce value into the return nonce structure
      g_nonce.nonce  = g_blk_hdr.nonce;

      // Report hash results and performance statistics
      c_nonce.send(&g_nonce, sizeof(g_nonce));
      c_perf.send(&g_stats, sizeof(g_stats));

	  } // end loop

	} //Process_Block_Header.main()

}; //Process_Block_Header()



behavior Event_Reset(int command) {
	void main(void) {
		command = 1;
	}
};

behavior Event_Abort(int command) {
	void main(void) {
		command = 2;
	}
};

behavior Event_Tout(int command) {
	void main(void) {
		command = 3;
	}
};

behavior HW_Hash(i_receiver  c_blk_hdr,
	               i_sender    c_nonce,
	               i_sender    c_perf,
	               event       e_abort,
	               event       e_ready,
	               event       e_reset,
	               event       e_tout,
	               event       e_tstart,
	               event       e_tstop) {

  int command;

  Return_Nonce timeout_nonce = {0xffffff, false};
  Statistics timeout_statistics = {0, 0, 0, 0, 0, 0, 0, 0};

  Process_Block_Header process_block_header(c_blk_hdr,
                                            c_nonce,
                                            c_perf,
                                            e_tstart,
                                            e_tstop);

  Event_Reset event_reset(command);

  Event_Abort event_abort(command);

  Event_Tout event_tout(command);

  void main(void) {
    while (true) {
    	printf("\nHW_Hash:wait(e_ready)");
  	  wait(e_ready);
    	while (true) {
    		printf("\nHW_Hash:try-trap");
    		try             {process_block_header.main();}
    		trap (e_reset)  {event_reset.main();}
    		trap (e_abort)  {event_abort.main();}
    		trap (e_tout)   {event_tout.main();}

    		if (command == 1) {
    		  printf("\nHW_Hash:Reset");
    			break;
    		}
    		else if (command == 2) {
    		  printf("\nHW_Hash:Abort");
    		}
    		else if (command == 3) {
    		  printf("\nHW_Hash:Timeout");
          c_nonce.send(&timeout_nonce, sizeof(timeout_nonce));
          //c_perf.send(&g_stats, sizeof(g_stats));    // DJK: Still need to figure
                                                       //      out how to send stats
                                                       //      in this case (and abort);
                                                       //      may need to pass these
                                                       //      in and out of behaviors
                                                       //      through port.
    		}
    	}
    }

  } // end HW_Hash.main();

}; // end HW_Hash()
