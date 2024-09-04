# Build this for maven, since the plugin is really hinky.
# Run it from the command line
# Make sure you "reload from disk" in the generated directory!!
# THEN rebuild the *entire project.
###########
# NOTE: ANTLR 4.10 generates several additional files. Use it the listeners need
# to be at least partly rewritten.
###########
ANTLR4_ROOT=/home/ncsa/apps/java/antlr-4.9.3
ANTLR4_CP=$ANTLR4_ROOT/antlr-4.9.3-complete.jar
OUT_DIR="/home/ncsa/dev/ncsa-git/qdl/language/src/main/java/org/qdl_lang/generated"
SOURCE_DIR="/home/ncsa/dev/ncsa-git/qdl/language/src/main/antlr4"
OUT_PACKAGE="org.qdl_lang.generated"
cd $SOURCE_DIR
# Stupidly, the antlr tool only builds from the current directory.
# There is no option to set the source directory
echo "switching to " $SOURCE_DIR
echo "putting files in "$OUT_DIR
antlr4 QDLParser.g4 -o $OUT_DIR -package $OUT_PACKAGE
cd $OUT_DIR
javac -cp $ANTLR4_CP: QDL*.java
