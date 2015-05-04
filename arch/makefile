#
# Makefile: Parity Generator
#

SCC = scrc

SCCOPT  = -vv -g -ww

all: tb

tb: tb.sc design.sc stimulus.sc monitor.sc pe1.sc pe2.sc pe3.sc pe4.sc \
	  core.sc rpcclient.sc rpcserver.sc networkwallet.sc hardwarewallet.sc \
		miningsw.sc hw_miner.sc hw_hash.sc hw_abort.sc hw_timer.sc hw_config.sc
	$(SCC) tb $(SCCOPT)

clean:
	-rm -f *~ *.o *.cc *.h
	-rm -f *.si
