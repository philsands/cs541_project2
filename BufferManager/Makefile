JDKPATH = /usr
LIBPATH = lib/bufmgrAssign.jar

CLASSPATH = .:..:$(LIBPATH)
BINPATH = bin
JAVAC = $(JDKPATH)/bin/javac 
JAVA  = $(JDKPATH)/bin/java 

PROGS = bufmgr diskmgr tests

all: $(PROGS)

bufmgr:
	$(JAVAC) -cp $(CLASSPATH) src/bufmgr/*.java
diskmgr:
	$(JAVAC) -cp $(CLASSPATH):bin src/diskmgr/*.java
tests:
	$(JAVAC) -cp $(CLASSPATH):bin src/tests/*.java

bmtest: 
	$(JAVA) -cp $(CLASSPATH):bin tests.BMTest

