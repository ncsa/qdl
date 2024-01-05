#
# Run this AFTER build.sh or it will fail.
#
QDL_ROOT=$NCSA_DEV_INPUT/qdl

GITHUB_ROOT=$QDL_ROOT/docs
echo "starting conversion"
./convert-docs.sh $QDL_ROOT/language/src/main/docs $GITHUB_ROOT/pdf
# cd $GITHUB_ROOT/pdf
# echo "converting docs to PDF"
#
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/iso6429.odt              > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_configuration.odt    > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_extensions.odt       > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_ini_file.odt         > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_reference.odt        > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_scripting.odt        > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_server_scripts.odt   > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/old-module-reference.odt > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_swing_gui.odt        > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_workspace.odt        > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/tutorial.odt             > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdldb-extension.odt      > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/http-extension.odt       > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/xml-extension.odt        > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/cli-extension.odt        > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/crypto.odt               > /dev/null
# lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_mail.odt             > /dev/null
# echo "done converting PDFs"

# ===============
cd $QDL_ROOT
mvn clean javadoc:javadoc -Dmaven.javadoc.skip=false
mvn javadoc:aggregate
cd $QDL_ROOT/website
mvn clean site

cp -r $QDL_ROOT/website/target/site/* $GITHUB_ROOT
cp -r $QDL_ROOT/target/site/* $GITHUB_ROOT
