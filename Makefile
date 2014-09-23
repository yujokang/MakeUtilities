.PHONY:classes docs

JAVAC=javac
JAR=jar
JAVADOC=javadoc

JARFLAGS=cf

ROOT_DIR=de/yjk/
UTILS_DIR=${ROOT_DIR}utils/
CGEN_DIR=${ROOT_DIR}cgen/

PUBLIC_CLASSES=${UTILS_DIR}FileFormatter.class ${CGEN_DIR}ArchiveTarget.class \
	${CGEN_DIR}BinaryTarget.class ${CGEN_DIR}Mainfile.class \
	${CGEN_DIR}Makefile.class ${CGEN_DIR}Target.class

SOURCES=${PUBLIC_CLASSES:.class=.java}
CLASSES=${PUBLIC_CLASSES} ${UTILS_DIR}FileFormatter\$$UnindentException.class \
	${CGEN_DIR}MakeFormatter.class \
	${CGEN_DIR}Makefile\$$NotDirectoryException.class \
	${CGEN_DIR}Mainfile\$$NotDescendantException.class

TARGETS=make_utils.jar

all: classes ${TARGETS}

classes: ${SOURCES}
	${JAVAC} ${SOURCES}

make_utils.jar: classes
	${JAR} cf $@ ${CLASSES}

docs:
	${JAVADOC} ${SOURCES}

clean:
	rm -rf ${CLASSES} ${TARGETS}
