/****************************************************************************
*  Title: hw_hash.sc
*  Author: Team 4
*  Date: 05/06/2015
*  Description: Bitcoin mining hardware behavior.  This encapsulates a
*    C-language SHA-256 algorithm in SpecC simulation code to represent
*    a complete Bitcoin transaction verification "component" (typically
*    an ASIC device).  Almost all measurement instrumentation used across
*    the project is found here, measuring time and power.
*
*    The SHA-256 C-language algorithm is derived from an open source
*    project by Kolivas and Gay:
*
* FIPS 180-2 SHA-224/256/384/512 implementation
* Last update: 02/02/2007
* Issue date:  04/30/2005
*
* Copyright (C) 2013, Con Kolivas <kernel@kolivas.org>
* Copyright (C) 2005, 2007 Olivier Gay <olivier.gay@a3.epfl.ch>
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
* 3. Neither the name of the project nor the names of its contributors
*    may be used to endorse or promote products derived from this software
*    without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
* LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
* OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
*
****************************************************************************/

#include <sim.sh>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#include "../api/coreapi.h"
#include "../config/hwconfig.h"

import "c_double_handshake";
import "tlmbus";

// The "work" construct is used to bridge differences between the
// way Bitcoin transaction "blocks" are delivered to this package
// with the mechanism used by the open source SHA-256 algorithm.
typedef struct {
	unsigned char root[32];
  unsigned char data[32];
	unsigned char hash[32];
} Work;


typedef struct {
  unsigned int nonce;
	bool status;
} Return_Nonce;

HWConfig hwconfig;

unsigned long t_clock;
unsigned long t_mread;
unsigned long t_mwrite;
unsigned long t_call;
unsigned long t_isum;
unsigned long t_imul;
unsigned long t_idiv;
unsigned long t_shft;
unsigned long t_rot;
unsigned long t_band;
unsigned long t_bor;
unsigned long t_bnot;
unsigned long t_bxor;
unsigned long t_comp;
unsigned long t_timeout;
unsigned long p_pwr_static;
unsigned long p_pwr_dynamic;

double t_period = 1.0;

BlockHeader g_blk_hdr;
Return_Nonce g_nonce;
PerformanceData g_stats;


void power_meter_cycles(unsigned long power, long cycles) {
	g_stats.cum_energy += (double)cycles * t_period * (double)power;
}

void power_meter_seconds(unsigned long power, double seconds) {
	g_stats.cum_energy += seconds * (double)power;
}


void configure_pe() {
	t_clock       = hwconfig.clock;
  t_mread       = hwconfig.mread;
  t_mwrite      = hwconfig.mwrite;
  t_call        = hwconfig.call;
  t_isum        = hwconfig.isum;
  t_imul        = hwconfig.imul;
  t_idiv        = hwconfig.idiv;
  t_shft        = hwconfig.shft;
  t_rot         = hwconfig.rot;
  t_band        = hwconfig.band;
  t_bor         = hwconfig.bor;
  t_bnot        = hwconfig.bnot;
  t_bxor        = hwconfig.bxor;
  t_comp        = hwconfig.comp;
  t_timeout     = hwconfig.timeout;
  p_pwr_static  = hwconfig.pwr_static;
  p_pwr_dynamic = hwconfig.pwr_dynamic;

  if (t_clock > 0) {
		t_period = 1.0 / (double)t_clock;
	}

} // end configure_pe()


behavior Process_Block_Header(IProtocolSlave   miner_bus,
			                        i_sender         c_perf,
			                        event            e_tstart,
			                        event            e_tstop) {


	long i_cycles;


	//***********************************//
	//** INSTRUMENTED CODE STARTS HERE **//
	//***********************************//


	//-----------------------------------------------------------------------
	// Constant: Process_Block_Header.sha256_init_state
	//-----------------------------------------------------------------------
  const unsigned int sha256_init_state[8] = {
  	0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
  	0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
  };


	//-----------------------------------------------------------------------
	// Function: General hashing utilities
	//-----------------------------------------------------------------------

  typedef unsigned int u32;
  typedef unsigned char u8;

  u32 ror32(u32 word, unsigned int shift) {
  	// Start Instrumentation
  	i_cycles = t_shft*(32-shift+1) + t_bor;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	return (word >> shift) | (word << (32 - shift));
  }


  u32 Ch(u32 x, u32 y, u32 z) {
  	// Start Instrumentation
  	i_cycles = t_bxor*2 + t_band;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	return z ^ (x & (y ^ z));
  }


  u32 Maj(u32 x, u32 y, u32 z) {
  	// Start Instrumentation
  	i_cycles = t_band*2 + t_bor*2;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	return (x & y) | (z & (x | y));
  }

  // Instrumentation:	i_cycles += t_call*3 + t_bxor*2  // e0
  #define e0(x) (ror32(x, 2) ^ ror32(x,13) ^ ror32(x,22))

  // Instrumentation:	i_cycles += t_call*3 + t_bxor*2  // e1
  #define e1(x) (ror32(x, 6) ^ ror32(x,11) ^ ror32(x,25))

  // Instrumentation:	i_cycles += t_call*3 + t_shft*3 + t_bxor*2  // s0
  #define s0(x) (ror32(x, 7) ^ ror32(x,18) ^ (x >> 3))

  // Instrumentation:	i_cycles += t_call*3 + t_shft*10 + t_bxor*2  // s1
  #define s1(x) (ror32(x,17) ^ ror32(x,19) ^ (x >> 10))

  // Instrumentation:	i_cycles += t_shft*16 + t_band +t_bor*2  // bswap_16
  #define	bswap_16(value)  \
 	  ((((value) & 0xff) << 8) | ((value) >> 8))

 	// Instrumentation:	i_cycles += t_call*2 + t_band + t_shft*32  // bswap_32
  #define	bswap_32(value)	\
   	(((unsigned int)bswap_16((unsigned short)((value) & 0xffff)) << 16) | \
   	(unsigned int)bswap_16((unsigned short)((value) >> 16)))


  void LOAD_OP(int I, u32 *W, const u8 *input) {
  	// Start Instrumentation
  	i_cycles = t_mread + t_mwrite;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	W[I] = ( ((u32*)(input))[I] );
  }


  void BLEND_OP(int I, u32 *W) {
  	// Start Instrumentation
  	i_cycles  = t_isum*7 + t_mread*4 + t_mwrite;
  	i_cycles += t_call*3 + t_shft*3 + t_bxor*2;   // s0 macro
  	i_cycles += t_call*3 + t_shft*10 + t_bxor*2;  // s1 macro
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	W[I] = s1(W[I-2]) + W[I-7] + s0(W[I-15]) + W[I-16];
  }


  unsigned int swab32(unsigned int v) {
  	// Start Instrumentation
  	i_cycles = t_call;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	return bswap_32(v);
  }


	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.sha256_transform
	//-----------------------------------------------------------------------
  void sha256_transform(u32 *state,
                        const u8 *input) {

  	u32 a, b, c, d, e, f, g, h, t1, t2;
  	u32 W[64];
  	int i;

  	/* load the input */
  	// Start Instrumentation
  	i_cycles = t_call*16 + t_comp*16 + t_isum*16;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	for (i = 0; i < 16; i++)
  		LOAD_OP(i, W, input);

  	/* now blend */
  	// Start Instrumentation
  	i_cycles = t_call * 48 + t_comp*48 + t_isum*48;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	for (i = 16; i < 64; i++)
  		BLEND_OP(i, W);

  	/* load the state into our registers */
  	// Start Instrumentation
  	i_cycles = t_mread*8 + t_mwrite*8;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	a=state[0];  b=state[1];  c=state[2];  d=state[3];
  	e=state[4];  f=state[5];  g=state[6];  h=state[7];

  	/* now iterate */

  	// Start Instrumentation
  	i_cycles  = (t_isum*4)*64 + t_call + t_mread; // For statements of the form "t1 = ..."
  	i_cycles += (t_isum*3)*64 + t_call + t_mread; // For statements of the form "t2 = ..."
  	i_cycles += (t_call*3 + t_bxor*2)*64;         // macro e0
  	i_cycles += (t_call*3 + t_bxor*2)*64;         // macro e1
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
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

  	// Start Instrumentation
  	i_cycles = t_isum*8 + t_mread*8 + t_mwrite*8;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	waitfor(t_isum*8);
  	state[0] += a; state[1] += b; state[2] += c; state[3] += d;
  	state[4] += e; state[5] += f; state[6] += g; state[7] += h;

  } // end Process_Block_Header.sha256_transform()



	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.runhash
	//-----------------------------------------------------------------------
  void runhash(void *state,
               const void *input,
               const void *init) {

    // Initialize the state array (32 bytes) from the init parameter
  	// Start Instrumentation
  	i_cycles = t_mread*32 + t_mwrite*32;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	memcpy(state, init, 32);

  	// Run the hash algorithm
  	// Start Instrumentation
  	i_cycles = t_call;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	sha256_transform((u32 *)state, (u8 *)input);

  } // end Process_Block_Header.runhash()



	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.swap256
	//-----------------------------------------------------------------------
  void swap256(void *dest_p, const void *src_p) {

		unsigned int *dest;
		unsigned int *src;

  	// Start Instrumentation
  	i_cycles = t_mread*2 + t_mwrite*2;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
		dest = (unsigned int *)dest_p;
		src = (unsigned int *)src_p;

  	// Start Instrumentation
  	i_cycles = t_mread*8 + t_mwrite*8;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
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

  	// Start Instrumentation
  	i_cycles = t_mread*2 + t_mwrite*2;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
		hash32 = (unsigned int *) hash_swap;
		target32 = (unsigned int *) target_swap;

  	// Start Instrumentation
  	i_cycles = t_call*2;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	swap256(hash_swap, hash);
  	swap256(target_swap, target);

  	// Start Instrumentation
  	i_cycles = t_comp*8 + t_isum*8;
  	waitfor(i_cycles);
  	power_meter_cycles(p_pwr_dynamic, i_cycles);
  	// End Instrumentation
  	for (i = 0; i < 32/4; i++) {

  	  // Start Instrumentation
  	  i_cycles = t_mread*2 + t_mwrite*2 + t_call;
  	  waitfor(i_cycles);
  	  power_meter_cycles(p_pwr_dynamic, i_cycles);
  	  // End Instrumentation
  		h32tmp = swab32(hash32[i]);
  		t32tmp = target32[i];

      // DJK: I don't think this is needed?
  		//target32[i] = swab32(target32[i]);	/* for printing */

  	  // Start Instrumentation
  	  i_cycles = t_comp;
  	  waitfor(i_cycles);
  	  power_meter_cycles(p_pwr_dynamic, i_cycles);
  	  // End Instrumentation
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



	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.scanhash
	//-----------------------------------------------------------------------
  bool scanhash(unsigned char *root,
                unsigned char *data,
  	            unsigned char *hash,
  	            unsigned char *target,
  	            unsigned int   max_nonce,
  	            unsigned long *hashes_done) {

  	unsigned int *hash32;
  	unsigned int *nonce;
  	unsigned int  n        = 0;
  	unsigned long stat_ctr = 0;
    unsigned char hash1[32];

		hash32   = (unsigned int *) hash;
		nonce    = (unsigned int *)(data + 12);

  	while (true) {

  	  // Start Instrumentation
  	  i_cycles = t_isum + t_mwrite;
  	  waitfor(i_cycles);
  	  power_meter_cycles(p_pwr_dynamic, i_cycles);
  	  // End Instrumentation
  		n++;
  		*nonce = n;

  	  // Start Instrumentation
  	  i_cycles = t_call*2;
  	  waitfor(i_cycles);
  	  power_meter_cycles(p_pwr_dynamic, i_cycles);
  	  // End Instrumentation
  		runhash(hash1, data, root);
  		runhash(hash, hash1, sha256_init_state);

  	  // Start Instrumentation
  	  i_cycles = t_isum + t_call;
  	  waitfor(i_cycles);
  	  power_meter_cycles(p_pwr_dynamic, i_cycles);
  	  // End Instrumentation
  		stat_ctr++;

  		if (stat_ctr % 10000 == 0) printf("hashing: nonce = %d\n", stat_ctr);

  		if (fulltest(hash, target)) {
  			// Start Instrumentation
  	    i_cycles = t_mwrite*2;
  	    waitfor(i_cycles);
  	    power_meter_cycles(p_pwr_dynamic, i_cycles);
  	    // End Instrumentation
  			*hashes_done = stat_ctr;
  			printf("HW_Hash.scanhash(VALID):nonce = %d\n", n);
  			g_blk_hdr.nonce = n;
  			//exit(0);
  			return true;
  		}
  		else if (n >= max_nonce) {
  	    // Start Instrumentation
  	    i_cycles = t_comp + t_mwrite;
  	    waitfor(i_cycles);
  	    power_meter_cycles(p_pwr_dynamic, i_cycles);
  	    // End Instrumentation
  			*hashes_done = stat_ctr;
  			return false;
  		}

  	} // end loop

  } // end Process_Block_Header.scanhash()



	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.hash_it_to_pieces
	//-----------------------------------------------------------------------
  bool hash_it_to_pieces() {

	  unsigned int max_nonce = 0xffffff;
    unsigned long hashes_done;
	  unsigned int t_start, t_end, diff;
	  bool rc;
	  Work work;
    int i;

    // Set to a small value to allow hash to complete
    // more quickly, for test purposes
    unsigned char target[32] = {
      0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
      0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
      0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
      0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0x00, 0x00
    };

    // Build "work" structure for testing
    for (i = 0; i < 32; i++) {
    	work.root[i] = (unsigned char) (rand() % 256);  // Random for testing
    	work.data[i] = g_blk_hdr.prev_hash[i];          // From SW_Miner
    }

    // Initialize hash attempt counter and record start time
	  hashes_done = 0;

	  // Scan nonces for a proof-of-work hash (returns true if valid)
    rc = scanhash(work.root,      // In,  Merkle Root
                  work.data,      // In,  Previous Block Hash
			            work.hash,      // Out, Resulting Hash
			            target,         // In,  Validation Threshold
				          max_nonce,      // In,  Max Iterations (effectively)
				          &hashes_done);  // Out, Number of Iterations

	  // Report results
	  return rc;

	} // end Process_Block_Header.hash_it_to_pieces()


	//**********************************//
	//** INSTRUMENTED CODE STOPS HERE **//
	//**********************************//

  void init_stats() {
    g_stats.cum_time      = 0.0;
    g_stats.cum_idle_time = 0.0;
    g_stats.cum_proc_time = 0.0;
    g_stats.cum_blocks    = 0.0;
    g_stats.cum_hashes    = 0.0;
    g_stats.cum_energy    = 0.0;
	  g_stats.mhash_per_j   = 0.0;
    g_stats.mhash_per_s   = 0.0;
  } // end init_stats()


	//-----------------------------------------------------------------------
	// Function: Process_Block_Header.main
	//-----------------------------------------------------------------------
	void main(void) {

    unsigned long long start_idling;
    unsigned long long start_processing;
	  double idle_time;
	  double processing_time;

	  init_stats();
	  start_idling = now();

		while (true) {

      // Wait for a block header to process (idle time)
      printf("HW_Hash:c_blk_hdr.receive()\n");
			miner_bus.slaveRead(C2, &g_blk_hdr, sizeof(g_blk_hdr));
      g_stats.cum_blocks++;
      printf("HW_Hash:blk_hdr received\n");

   	  // Start Instrumentation
      idle_time = (double)(now() - start_idling) * t_period;
      g_stats.cum_idle_time += idle_time;
      g_stats.cum_time += idle_time;
      power_meter_seconds(p_pwr_static, idle_time);
      start_processing = now();
  	  // End Instrumentation

      // Run the hash search function, using the watchdog timer to
      // prevent the search from running endlessly
      notify(e_tstart); // Start watchdog
      g_nonce.status = hash_it_to_pieces();
      notify(e_tstop);  // Stop watchdog

  	  // Start Instrumentation
      processing_time = (double)(now() - start_processing) * t_period;
      g_stats.cum_proc_time += processing_time;
      g_stats.cum_time += processing_time;
  	  // End Instrumentation

      g_stats.cum_hashes += (double)g_blk_hdr.nonce;
      if (g_stats.cum_energy != 0.0) {
        g_stats.mhash_per_j =
          ((double)g_stats.cum_hashes / g_stats.cum_energy) / 1000000.0;
      }
      if (g_stats.cum_time != 0.0) {
        g_stats.mhash_per_s =
          ((double)g_stats.cum_hashes / g_stats.cum_time) / 1000000.0;
      }
  	  // End Instrumentation

      // Set the final nonce value into the return nonce structure
      g_nonce.nonce  = g_blk_hdr.nonce;

      // Report hash results and performance statistics
	  miner_bus.slaveWrite(C3, &g_nonce, sizeof(g_nonce));
      start_idling = now();
      g_stats.flag = 0;  // Valid perf block
      c_perf.send(&g_stats, sizeof(g_stats));

	  } // end loop

	} //Process_Block_Header.main()

}; //Process_Block_Header()



behavior Event_Reset(int   command,
                     event e_tstop) {
	void main(void) {
		notify(e_tstop);
		command = 1;
	}
};

behavior Event_Abort(int command,
                     event e_tstop) {
	void main(void) {
		notify(e_tstop);
		command = 2;
	}
};

behavior Event_Tout(int command) {
	void main(void) {
		command = 3;
	}
};

behavior HW_Hash(IProtocolSlave   miner_bus,
	               i_sender         c_perf,
                 i_receiver       c_pe_profile,
	               event            e_abort,
	               event            e_reset,
	               event            e_tout,
	               event            e_tstart,
	               event            e_tstop) {

  int command;

  Return_Nonce timeout_nonce = {0xffffff, false};
  PerformanceData timeout_statistics = {0, 0, 0, 0, 0, 0, 0, 0};

  Process_Block_Header process_block_header(miner_bus,
	                                          c_perf,
	                                          e_tstart,
	                                          e_tstop);

  Event_Reset event_reset(command, e_tstop);

  Event_Abort event_abort(command, e_tstop);

  Event_Tout event_tout(command);

  void main(void) {
    while (true) {
    	printf("HW_Hash:c_pe_profile.receive()\n");
  	  c_pe_profile.receive(&hwconfig, sizeof(hwconfig));
  	  configure_pe();
  	  printf("HW_Hash:pwr_static = %lu, pwr_dynamic = %lu\n", p_pwr_static, p_pwr_dynamic);
    	while (true) {
    		try             {process_block_header.main();}
    		trap (e_reset)  {event_reset.main();}
    		trap (e_abort)  {event_abort.main();}
    		trap (e_tout)   {event_tout.main();}

    		if (command == 1) {
    		  printf("HW_Hash:Reset\n");
    			break;
    		}
    		else if (command == 2) {
    		  printf("HW_Hash:Abort\n");
					miner_bus.slaveWrite(C3, &timeout_nonce, sizeof(timeout_nonce));
					g_stats.flag = 1;  // Abort
					c_perf.send(&g_stats, sizeof(g_stats));
    		}
    		else if (command == 3) {
    		  printf("HW_Hash:Timeout\n");
		  miner_bus.slaveWrite(C3, &timeout_nonce, sizeof(timeout_nonce));
          g_stats.flag = 2;  // Timeout
          c_perf.send(&g_stats, sizeof(g_stats));
    		}
    	}
    }

  } // end HW_Hash.main();

}; // end HW_Hash()
