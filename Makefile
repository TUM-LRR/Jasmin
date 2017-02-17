JAVAC := javac
JAVA := java

SRCDIR := src
TARGETDIR := "build"
JARDIR := bin
TARGET := $(TARGETDIR)/Main.class
TARGETJAR := $(JARDIR)/jasmin.jar

CFLAGS := -d $(TARGETDIR)

SRCEXT := java
SOURCETXT := sources.txt

SOURCES := $(shell cat ${SOURCETXT})

RESOURCESSRC := $(SRCDIR)/jasmin/gui/resources
RESOURCESDST := $(TARGETDIR)/jasmin/gui

#ifeq ($(OS),Windows_NT)
#	FINDCMD := dir /s /B *.$(SRCEXT) > $(SOURCETXT)
#else
FINDCMD := find -name "*.$(SRCEXT)" > $(SOURCETXT)
#endif

all: $(TARGETJAR)
	
test:
	@echo "TODO automated tests"

$(TARGETJAR): $(JARDIR) build
	jar cvfe $(TARGETJAR) jasmin.Main -C $(TARGETDIR) .
	
build: $(TARGET) $(RESOURCESDST)

$(TARGET): $(TARGETDIR) $(SOURCETXT) $(RESOURCESSRC)
	$(JAVAC) $(CFLAGS) @$(SOURCETXT)

$(RESOURCESDST):
	rsync -rupE $(RESOURCESSRC) $(RESOURCESDST)

#$(RESOURCESDST): $(RESOURCESSRC)
#	mkdir -p $(@D)
#	cp $< $@

$(JARDIR):
	mkdir -p $(JARDIR)

$(TARGETDIR):
	mkdir -p $(TARGETDIR)

$(SOURCETXT):
	rm -f $(SOURCETXT)
	$(FINDCMD)

clean:
	rm -f -rf $(TARGETDIR)
	rm -f -rf $(JARDIR)
	rm -f sources.txt
	
.PHONY: $(SOURCETXT) $(RESOURCESDST) $(TARGETDIR) $(JARDIR)