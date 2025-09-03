#
# Run this AFTER build.sh or it will fail.
#
QDL_ROOT=$NCSA_DEV_INPUT/qdl

GITHUB_ROOT=$QDL_ROOT/docs
echo "starting conversion"
$QDL_ROOT/website/convert-docs.sh $QDL_ROOT/language/src/main/docs $GITHUB_ROOT/pdf

cd $QDL_ROOT || exit
mvn clean javadoc:javadoc -Dmaven.javadoc.skip=false
mvn javadoc:aggregate
cd $QDL_ROOT/website || exit
mvn  site

cp -r $QDL_ROOT/website/target/site/* $GITHUB_ROOT
cp -r $QDL_ROOT/target/site/* $GITHUB_ROOT
