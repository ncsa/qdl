#Build qdl
# When this script is done, qdl will have been created, documentation created
# and the target directory specified in the create_installer.sh script will have
# the qdl installer jar.

QDL_ROOT=$NCSA_DEV_INPUT/qdl

# Uncomment the next two lines if you want/need to regenerate all of the parser.
# Generally, you should rarely need to do this  since  again, doing this implies a
# change to the language itself
# Note that the binary files are included in this distribution.

# cd $QDL_ROOT/language/src/main/antlr
# ./build.sh

# Uncomment the next two lines to regenerate the parser for the
# ini files. Again, doing this implies a change to the grammar
# since the binary files are included in this distribution.

# cd $QDL_ROOT/language/src/main/antlr/iniFile
# ./build.sh

# build qdl proper
echo "building QDL from sources..."
cd $QDL_ROOT || exit
mvn clean install > maven.log
if [ $? -ne 0 ]
then
  echo "error running maven. Check maven.log" >&2
  exit 1;
fi
echo "     ... done!"
echo "  creating installer"
cd "$QDL_ROOT"/language/src/main/scripts || exit
./create_installer.sh > installer.log
if [ $? -ne 0 ]
then
  echo "error running installer creation. Check installer.log" >&2
  exit 1;
fi
echo "     ... done!"


