#Build qdl
# When this script is done, qdl will have been created, documentation created
# and the target directory specified in the create_installer.sh script will have
# the qdl installer jar.

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
DEPLOY_ROOT=/home/ncsa/dev/temp-deploy
QDL_ROOT=/home/ncsa/dev/ncsa-git/qdl

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
