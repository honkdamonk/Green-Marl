CC = g++
GEN_SRC = ../../apps/output_cpp/generated
INCDIR = ../../apps/output_cpp/gm_graph/inc
LIBDIR = ../../apps/output_cpp/gm_graph/lib
BINDIR = ./bin
LIB = gmgraph
CFLAGS = -O3 -g -I$(GEN_SRC) -I$(INCDIR) -I. -fopenmp -Wno-write-strings
LFLAGS = -L$(LIBDIR) -l$(LIB)
INCFILES = $(shell ls $(INCDIR))
H_FILES = $(addprefix $(INCDIR)/, $(INCFILES))

include ../../setup.mk
include ../../apps/output_cpp/common.mk

PROGS = pagerank hop_dist kosaraju
OUT = $(addprefix $(BINDIR)/, $(PROGS))

LFILES = $(addprefix $(GEN_SRC)/, $(PROGS))
L_FILES = $(addsuffix .cc, $(LFILES))

all: $(OUT)

echo:
	echo $(INCFILES)

$(BINDIR)/% : %_test.cc $(LIBDIR)/libgmgraph.a 
	$(CC) $(CFLAGS) $(L_FILES) $^ $(LFLAGS) -o $@_test
	
clean:
	\rm -f $(OUT)
