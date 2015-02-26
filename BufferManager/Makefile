JDKPATH = C:/Program Files/Java/jdk1.7.0_45
LIBPATH = lib/bufmgrAssign.jar

CLASSPATH = $(LIBPATH)
BINPATH = $(JDKPATH)/bin
JAVAC = $(JDKPATH)/bin/javac 
JAVA  = $(JDKPATH)/bin/java 

PROGS = xx

all: $(PROGS)

compile:src/*/*.java
	$(JAVAC) -cp $(CLASSPATH) -d bin src/bufmgr/*.java
	$(JAVAC) -cp $(CLASSPATH) -d bin src/diskmgr/*.java
	$(JAVAC) -cp $(CLASSPATH) -d bin src/tests/*.java
	

xx : compile
	$(JAVA) -cp $(CLASSPATH):bin tests.BMTest

