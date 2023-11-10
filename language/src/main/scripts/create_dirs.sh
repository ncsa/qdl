# This creates the directory structure for creating QDL from the sources.
#
#
DEFAULT_QDL_ROOT=$NCSA_DEV_INPUT/qdl
QDL_SOURCES=$NCSA_DEV_INPUT/qdl/language
DEFAULT_TARGET_ROOT=$NCSA_DEV_OUTPUT/qdl
DEFAULT_JAR_NAME="qdl-installer.jar"

if [[  "$1" = "--help" ]];then
  echo "create_dirs.sh [qdl_root target_dir]"
  echo "create the diectory structure and populate it for QDL. You may then run the"
  echo "create_installer.sh to create the actual jar."
  echo "No arguments means to use the qdl root (assumes there is already a qdl.jar there) named '$DEFAULT_QDL_ROOT'"
   echo "and create the directories in   '$DEFAULT_TARGET_ROOT'"
   echo "The result will be a jar named '$DEFAULT_JAR_NAME"
  exit 1
fi


# **IF** there are arguments for the target of this, use them. Otherwise use the default
QDL_ROOT=${1:-$DEFAULT_QDL_ROOT}
TARGET_ROOT=${2:-$DEFAULT_TARGET_ROOT}

if [ ! -d "$TARGET_ROOT" ]
  then
    mkdir "$TARGET_ROOT"
   else
    echo "$TARGET_ROOT exists, cleaning..."
    cd $TARGET_ROOT
    rm -Rf *
fi

cd $TARGET_ROOT
rm -Rf *

mkdir edu
mkdir edu/uiuc
mkdir edu/uiuc/ncsa
mkdir edu/uiuc/ncsa/qdl
mkdir edu/uiuc/ncsa/qdl/install


cd $TARGET_ROOT
cp $QDL_SOURCES/src/main/scripts/installer.mf .
cp $QDL_SOURCES/src/main/scripts/version.txt .
cp $QDL_SOURCES/target/classes/edu/uiuc/ncsa/qdl/install/Installer.class edu/uiuc/ncsa/qdl/install

# Now make the directories
mkdir "bin"
cp $QDL_SOURCES/src/main/scripts/qdl bin
mkdir "docs"
cp $QDL_ROOT/docs/pdf/*.pdf docs
cp $QDL_SOURCES/src/main/docs/jsonpath.pdf docs

mkdir "etc"
cp $QDL_SOURCES/src/main/resources/min-cfg.xml etc/min-cfg.xml
cp $QDL_SOURCES/src/main/resources/nano/qdl.nanorc etc/qdl.nanorc
cp $QDL_SOURCES/src/main/resources/nano/qdl.nanorc-2.3.1 etc/qdl.nanorc-2.3.1
cp $QDL_SOURCES/src/main/resources/sample.ini etc/sample.ini
mkdir "etc/modules"
cp $QDL_SOURCES/src/main/resources/modules/math-x.mdl etc/modules
cp $QDL_SOURCES/src/main/resources/modules/ext.mdl etc/modules
cp $QDL_SOURCES/src/main/resources/modules/readme.txt etc/modules
mkdir "examples"
cp $QDL_SOURCES/src/main/resources/examples/*.qdl examples/
mkdir "lib"
mkdir "log"
mkdir "lib/cp"
mkdir "var"
mkdir "var/ws"
