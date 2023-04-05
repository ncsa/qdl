#
# Run this AFTER build.sh or it will fail.
#
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
QDL_ROOT=/home/ncsa/dev/ncsa-git/qdl

#GITHUB_ROOT=$QDL_ROOT/docs
# OLD location for now so the website updates
GITHUB_ROOT=/home/ncsa/dev/ncsa-git/qdl/docs
cd $GITHUB_ROOT/pdf
echo "converting docs to PDF"

lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/iso6429.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_configuration.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_extensions.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_ini_file.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_reference.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_scripting.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_server_scripts.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_swing_gui.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_workspace.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/tutorial.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdldb-extension.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/http-extension.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/xml-extension.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/cli-extension.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/crypto.odt
echo "done converting PDFs"

# ===============
cd $QDL_ROOT
mvn javadoc:aggregate
cd $QDL_ROOT/website
mvn clean site

cp -r $QDL_ROOT/website/target/site/* $GITHUB_ROOT
cp -r $QDL_ROOT/target/site/* $GITHUB_ROOT
