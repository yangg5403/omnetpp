#
# variables set by `configure':
#

CONFIGFILE = ../../Makefile.inc
include $(CONFIGFILE)

#
# Local definitions
#
O=$(OMNETPP_OUT_DIR)/$(CONFIGNAME)/src/utils
L=$(OMNETPP_LIB_DIR)

.PHONY : all clean
#
# Rules
#
all: $(CONFIGFILE)
	$(Q)echo "#!/bin/sh" >opp_configfilepath && echo 'echo `dirname $$0`/../Makefile.inc' >>opp_configfilepath
	$(Q)if [ "$$OS" = "Windows_NT" ] ; then \
	  echo "@echo $(OMNETPP_ROOT)/Makefile.inc" >opp_configfilepath.cmd && \
	  cp opp_configfilepath.cmd $(OMNETPP_BIN_DIR); \
	  cp opp_makemake.cmd $(OMNETPP_BIN_DIR); \
	  cp opp_runall.cmd $(OMNETPP_BIN_DIR); \
	  cp opp_test.cmd $(OMNETPP_BIN_DIR); \
	  cp omnetpp.cmd $(OMNETPP_BIN_DIR); \
	  cp omnest.cmd $(OMNETPP_BIN_DIR); \
	fi
	$(Q)cp opp_configfilepath $(OMNETPP_BIN_DIR)
	$(Q)cp opp_makemake $(OMNETPP_BIN_DIR)
	$(Q)cp opp_featuretool $(OMNETPP_BIN_DIR)
	$(Q)cp opp_charttool $(OMNETPP_BIN_DIR)
	$(Q)cp opp_test $(OMNETPP_BIN_DIR)
	$(Q)cp opp_fingerprinttest $(OMNETPP_BIN_DIR)
	$(Q)cp opp_shlib_postprocess $(OMNETPP_BIN_DIR)
	$(Q)cp opp_runall $(OMNETPP_BIN_DIR)
	$(Q)cp opp_neddoc $(OMNETPP_BIN_DIR)
	$(Q)cp splitvec $(OMNETPP_BIN_DIR)
	$(Q)cp omnetpp $(OMNETPP_BIN_DIR)
	$(Q)cp omnest $(OMNETPP_BIN_DIR)
	$(Q)chmod +x $(OMNETPP_BIN_DIR)/opp_makemake $(OMNETPP_BIN_DIR)/opp_test $(OMNETPP_BIN_DIR)/opp_fingerprinttest $(OMNETPP_BIN_DIR)/splitvec $(OMNETPP_BIN_DIR)/opp_configfilepath $(OMNETPP_BIN_DIR)/opp_shlib_postprocess $(OMNETPP_BIN_DIR)/opp_runall $(OMNETPP_BIN_DIR)/opp_neddoc $(OMNETPP_BIN_DIR)/omnetpp $(OMNETPP_BIN_DIR)/omnest

clean:
	$(qecho) Cleaning utils
	$(Q)rm -rf $O
	$(Q)cd $(OMNETPP_BIN_DIR) && rm -f opp_makemake opp_runall opp_neddoc opp_test opp_fingerprinttest splitvec

-include $(OBJS:%=%.d)

