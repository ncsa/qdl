#
# Run this AFTER build.sh or it will fail.
#
QDL_ROOT=$NCSA_DEV_INPUT/qdl

GITHUB_ROOT=$QDL_ROOT/docs
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
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_mail.odt
echo "done converting PDFs"

# ===============
cd $QDL_ROOT
mvn clean javadoc:javadoc -Dmaven.javadoc.skip=false
mvn javadoc:aggregate
cd $QDL_ROOT/website
mvn clean site

cp -r $QDL_ROOT/website/target/site/* $GITHUB_ROOT
cp -r $QDL_ROOT/target/site/* $GITHUB_ROOT
