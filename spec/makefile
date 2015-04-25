#
# Makefile: Parity Generator
#

SCC = scrc

SCCOPT  = -vv -ww -g

all: tb

tb: tb.sc design.sc stimulus.sc monitor.sc wallet.sc core.sc \
	miningsw.sc mininghw.sc
	$(SCC) tb $(SCCOPT)

clean:
	-rm -f *~ *.o *.cc *.h
	-rm -f *.si
