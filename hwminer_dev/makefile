#
# Makefile: Bitcoin Validation Component and Test Bench
#

SCC = scrc

SCCOPT  = -vv -ww -g

all: main

main: main.sc stimulus.sc monitor.sc sw_miner.sc hw_miner.sc hw_hash.sc hw_timer.sc hw_config.sc hw_abort.sc

	$(SCC) main $(SCCOPT)

clean:
	-rm -f *~ *.o *.cc *.h
	-rm -f *.si
