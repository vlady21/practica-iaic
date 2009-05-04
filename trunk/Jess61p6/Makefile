#
# Simple Jess Makefile, for a unix-like environment. Will need a bit of
# hacking to work with nmake on Win32. Use CygWin instead!
#

.SUFFIXES: .java .class 

JAVAC=javac
JAVA=java
JAVAFLAGS=

.java.class :
	$(JAVAC) $(JAVAFLAGS)  $<

SRC = $(shell find jess -name '*.java' -print)

OBJ = $(SRC:.java=.class)

Main: $(OBJ) 

clean: 
	-@ find . -name '*.class' -exec rm \{\} \;
	-@ find . -name '*~' -exec rm \{\} \;

