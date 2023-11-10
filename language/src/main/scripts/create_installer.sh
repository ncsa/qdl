# Accepts alternate jar name (e.g. qdl-installer.jar) as its argument.

QDL_SOURCES=$NCSA_DEV_INPUT/qdl/language
QDL_ROOT=$NCSA_DEV_INPUT/qdl
QDL_SOURCES=$NCSA_DEV_INPUT/qdl/language
TARGET_ROOT=$NCSA_DEV_OUTPUT/qdl
DEFAULT_JAR_NAME="qdl-installer.jar"

JAR_NAME=${1:-$DEFAULT_JAR_NAME}

create_dirs.sh $QDL_ROOT $TARGET_ROOT

cd $QDL_SOURCES
mvn -P qdl package
cp "$QDL_SOURCES/target/qdl.jar" $TARGET_ROOT/lib
unzip -p target/qdl.jar META-INF/MANIFEST.MF > $TARGET_ROOT/lib/build-info.txt

cd $DEFAULT_TARGET_ROOT
# Get the actual manifest so that build info is available.
jar cmf installer.mf "$JAR_NAME" edu/uiuc/ncsa/qdl/install/Installer.class version.txt  bin docs etc lib log var examples
