# Accepts alternate jar name (e.g. qdl-installer.jar) as its argument.
# The primary assumption is that you have run the build already with
# mvn clean install from the top-level and everythign is ready to go
#
# This just packages the executable, copies the build to the target directory
# and assembles the installer jar.

QDL_SOURCES=$NCSA_DEV_INPUT/qdl/language
QDL_ROOT=$NCSA_DEV_INPUT/qdl
TARGET_ROOT=$NCSA_DEV_OUTPUT/qdl
DEFAULT_JAR_NAME="qdl-installer.jar"

JAR_NAME=${1:-$DEFAULT_JAR_NAME}
echo 'creating QDL installer'
./create_dirs.sh $QDL_ROOT $TARGET_ROOT
if [ $? -ne 0 ]
then
  echo "error running create dirs. " >&2
  exit 1;
fi

cd $QDL_SOURCES  || exit
mvn -P qdl package > qdl-maven.log
if [ $? -ne 0 ]
then
  echo "error running maven. Check maven.log" >&2
  exit 1;
fi

cp "$QDL_SOURCES/target/qdl-jar-with-dependencies.jar" $TARGET_ROOT/lib/qdl.jar
unzip -p target/qdl-jar-with-dependencies.jar META-INF/MANIFEST.MF > $TARGET_ROOT/lib/build-info.txt

# Get the actual manifest so that build info is available.
cd $TARGET_ROOT || exit
java  edu.uiuc.ncsa.qdl.install.ListDistroFiles $TARGET_ROOT
jar cmf installer.mf "$JAR_NAME" edu/uiuc/ncsa/qdl/install/Installer.class version.txt dir_list.txt file_list.txt bin docs etc lib log var examples
echo '     ... done!'
