# This will create the installer (an executable jar file).
# Be sure you have run the build scripts first so there is stuff to copy.
#
DEFAULT_QDL_ROOT=$NCSA_DEV_INPUT/qdl
DEFAULT_TARGET_ROOT=$NCSA_DEV_OUTPUT/qdl
DEFAULT_JAR_NAME="qdl-installer.jar"

if [[  "$1" = "--help" ]];then
  echo "create_installer.sh [qdl_root target_dir jar_name]"
  echo "create the installable jar for qdl."
  echo "No arguments means to use the qdl root (assumes there is already a qdl.jar there) named '$DEFAULT_QDL_ROOT'"
   echo "and create the directories in   '$DEFAULT_TARGET_ROOT'"
   echo "The result will be a jar named '$DEFAULT_JAR_NAME"
  exit 1
fi


# **IF** there are arguments for the target of this, use them. Otherwise use the default
QDL_ROOT=${1:-$DEFAULT_QDL_ROOT}
TARGET_ROOT=${2:-$DEFAULT_TARGET_ROOT}
JAR_NAME=${3:-$DEFAULT_JAR_NAME}

echo "cleaning out old deploy in " $TARGET_ROOT
if [ ! -d "$TARGET_ROOT" ]
then
    echo "Directory $TARGET_DIR DOES NOT exists. Please create it."
    exit 1
fi

cd $TARGET_ROOT
rm -Rf *

mkdir edu
mkdir edu/uiuc
mkdir edu/uiuc/ncsa
mkdir edu/uiuc/ncsa/qdl
mkdir edu/uiuc/ncsa/qdl/install


cd $TARGET_ROOT
cp $QDL_ROOT/language/src/main/scripts/installer.mf .
cp $QDL_ROOT/language/src/main/scripts/version.txt .
cp $QDL_ROOT/language/target/classes/edu/uiuc/ncsa/qdl/install/Installer.class edu/uiuc/ncsa/qdl/install

# Now make the directories
mkdir "bin"
cp $QDL_ROOT/language/src/main/scripts/qdl bin
mkdir "docs"
cp $QDL_ROOT/docs/pdf/*.pdf docs
cp $QDL_ROOT/language/src/main/docs/jsonpath.pdf docs

mkdir "etc"
cp $QDL_ROOT/language/src/main/resources/min-cfg.xml etc/min-cfg.xml
cp $QDL_ROOT/language/src/main/resources/nano/qdl.nanorc etc/qdl.nanorc
cp $QDL_ROOT/language/src/main/resources/nano/qdl.nanorc-2.3.1 etc/qdl.nanorc-2.3.1
cp $QDL_ROOT/language/src/main/resources/sample.ini etc/sample.ini
mkdir "etc/modules"
cp $QDL_ROOT/language/src/main/resources/modules/math-x.mdl etc/modules
cp $QDL_ROOT/language/src/main/resources/modules/ext.mdl etc/modules
cp $QDL_ROOT/language/src/main/resources/modules/readme.txt etc/modules
mkdir "examples"
cp $QDL_ROOT/language/src/main/resources/examples/*.qdl examples/
mkdir "lib"
cp "$QDL_ROOT/language/target/qdl.jar" lib
cd lib
# Get the actual manifest so that build info is available.
unzip qdl.jar "*.MF"
# Puts it in the main qdl directory. Should be exactly one file in it.
mv META-INF/MANIFEST.MF build-info.txt
rmdir META-INF/

cd ..
mkdir "log"
mkdir "lib/cp"
mkdir "var"
mkdir "var/ws"
# jar cmf manifest-file jar-file input-files
#jar cmf installer.mf $JAR_NAME edu/uiuc/ncsa/qdl/install/Installer.class version.txt  build-info.txt bin docs etc lib log var examples
jar cmf installer.mf $JAR_NAME edu/uiuc/ncsa/qdl/install/Installer.class version.txt  bin docs etc lib log var examples
