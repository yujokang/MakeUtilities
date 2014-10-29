.PHONY:classes examples docs

JAVAC=javac
JAR=jar
JAVADOC=javadoc

JARFLAGS=cf

ROOT_DIR=de/yjk/
UTILS_DIR=${ROOT_DIR}utils/
CGEN_DIR=${ROOT_DIR}cgen/

EXAMPLES_DIR=examples/

PUBLIC_CLASSES=${UTILS_DIR}FileFormatter.class ${CGEN_DIR}ArchiveTarget.class \
	${CGEN_DIR}BinaryTarget.class ${CGEN_DIR}Mainfile.class \
	${CGEN_DIR}Makefile.class ${CGEN_DIR}Target.class

EXAMPLES_PUBLIC_CLASSES=${EXAMPLES_DIR}RecursivePopulate.class \
			${EXAMPLES_DIR}RecursivePopulate_2.class

SOURCES=${PUBLIC_CLASSES:.class=.java}
CLASSES=${PUBLIC_CLASSES} ${UTILS_DIR}FileFormatter\$$UnindentException.class \
	${CGEN_DIR}MakeFormatter.class \
	${CGEN_DIR}Makefile\$$NotDirectoryException.class \
	${CGEN_DIR}Mainfile\$$NotDescendantException.class

EXAMPLES_SOURCES=${EXAMPLES_PUBLIC_CLASSES:.class=.java}
EXAMPLES_CLASSES=${EXAMPLES_PUBLIC_CLASSES}

OUT_JAR=make_utils.jar

TARGETS=${OUT_JAR}

all: classes ${TARGETS} examples

classes: ${SOURCES}
	${JAVAC} ${SOURCES}

examples: ${EXAMPLES_SOURCES}
	${JAVAC} ${EXAMPLES_SOURCES}

make_utils.jar: classes
	${JAR} cf $@ ${CLASSES}

docs:
	${JAVADOC} ${SOURCES}

clean:
	rm -rf ${CLASSES} ${TARGETS} ${EXAMPLES_CLASSES}
