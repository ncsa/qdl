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
    echo "  deploy target directory exists, cleaning..."
    cd $TARGET_ROOT
    rm -Rf *
fi

cd $TARGET_ROOT
rm -Rf *

mkdir org
mkdir org.qdl_lang
mkdir org/qdl_lang/install


cd $TARGET_ROOT
cp $QDL_SOURCES/src/main/scripts/installer.mf .
cp $QDL_SOURCES/src/main/scripts/version.txt .
# following class has to be installed here so it executes later.
cp "$QDL_SOURCES"/target/classes/org/qdl_lang/install/*.class org/qdl_lang/install

# Now make the directories
mkdir "bin"
cp $QDL_SOURCES/src/main/scripts/qdl bin
cp $QDL_SOURCES/src/main/scripts/qdl-run bin
mkdir "docs"
# assumes the website was created
$QDL_ROOT/website/convert-docs.sh $QDL_ROOT/language/src/main/docs $TARGET_ROOT/docs
# cp $QDL_ROOT/docs/pdf/*.pdf docs
cp $QDL_SOURCES/src/main/docs/jsonpath.pdf docs

mkdir "etc"
cp $QDL_SOURCES/src/main/resources/nano/qdl.nanorc etc/qdl.nanorc
cp $QDL_SOURCES/src/main/resources/nano/qdl.nanorc-2.3.1 etc/qdl.nanorc-2.3.1
cp $QDL_SOURCES/src/main/resources/cfg-min.xml etc/cfg-min.xml

mkdir "etc/modules"
cp $QDL_SOURCES/src/main/resources/modules/anaphors.mdl etc/modules
cp $QDL_SOURCES/src/main/resources/modules/ext.mdl etc/modules
cp $QDL_SOURCES/src/main/resources/modules/math-x.mdl etc/modules
cp $QDL_SOURCES/src/main/resources/modules/readme.txt etc/modules
mkdir "etc/vim"
cp $QDL_SOURCES/src/main/resources/vim/readme.txt etc/vim
mkdir "etc/vim/ftdetect"
cp $QDL_SOURCES/src/main/resources/vim/ftdetect/qdl.vim etc/vim/ftdetect
mkdir "etc/vim/syntax"
cp $QDL_SOURCES/src/main/resources/vim/syntax/qdl.vim etc/vim/syntax


mkdir "examples"
cp $QDL_SOURCES/src/main/resources/examples/*.qdl examples/
cp $QDL_SOURCES/src/main/resources/cfg-ex.xml examples/cfg-ex.xml
cp $QDL_SOURCES/src/main/resources/sample.ini examples/sample.ini
mkdir "lib"
mkdir "log"
mkdir "lib/cp"
mkdir "var"
mkdir "var/ws"

