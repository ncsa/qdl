#Build qdl
# When this script is done, qdl will have been created, documentation created
# and the target directory specified in the create_installer.sh script will have
# the qdl installer jar.

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
DEPLOY_ROOT=/home/ncsa/dev/temp-deploy
QDL_ROOT=/home/ncsa/dev/ncsa-git/qdl

# convert pdfs and add to the documentation website.
GITHUB_ROOT=/home/ncsa/dev/ncsa-git/cilogon.github.io.git
cd $GITHUB_ROOT/qdl/docs
echo "converting docs to PDF"

lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/iso6429.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_configuration.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_reference.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_server_scripts.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_extensions.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_scripting.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_workspace.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/tutorial.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdl_ini_file.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/qdldb-extension.odt
lowriter --headless --convert-to pdf ~/dev/ncsa-git/qdl/language/src/main/docs/http-extension.odt
echo "done converting PDFs"

# Uncomment the next two lines if you want/need to regenerate all of the parser.
# Generally you should not need to do this ever:

# cd $QDL_ROOT/language/src/main/antlr
# ./build.sh

# build qdl proper
cd $QDL_ROOT
mvn clean install

cd $QDL_ROOT/language
mvn -P qdl package
mv target/qdl-jar-with-dependencies.jar target/qdl.jar
/home/ncsa/dev/ncsa-git/qdl/language/src/main/scripts/create_installer.sh

QDL_LOCAL_INSTALL=/home/ncsa/apps/qdl
OA2_LOCAL_INSTALL=/home/ncsa/apps/oa2
