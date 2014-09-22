.PHONY:classes

JAVAC=javac
JAR=jar

JARFLAGS=cf

ROOT_DIR=de/yjk/
UTILS_DIR=${ROOT_DIR}utils/
CGEN_DIR=${ROOT_DIR}cgen/

CLASSES=${UTILS_DIR}FileFormatter.class ${CGEN_DIR}ArchiveTarget.class \
	${CGEN_DIR}BinaryTarget.class ${CGEN_DIR}Mainfile.class \
	${CGEN_DIR}Makefile.class ${CGEN_DIR}Target.class

SOURCES=${CLASSES:.class=.java}

TARGETS=make_utils.jar

all: classes ${TARGETS}

classes: ${SOURCES}
	${JAVAC} ${SOURCES}

make_utils.jar: ${CLASSES}
	${JAR} cf $@ $^

clean:
	rm -rf ${CLASSES} ${TARGETS}
