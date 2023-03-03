JFlex is an Java successor to YACC and a few other parser generators. It
is used in various projects for its simplicity (such as Intellij or the
RSyntaxtTextArea component) but is not nearly as heavy weight as ANTLR.

There is a "TokenMakerMaker" that lets you fill in the blanks and generate a
JFlex grammar. This readme is about using that tool. There is no way to really
use an ANTLR grammar for this, since they work in fundamentally different ways.

>> In big blocks, load a syntax file in XML, update it, have it generate new classes. <<

NOTE that the tool described here runs under Java 14 or higher only, so switch JVM with

sudo update-alternatives --config java

To generate the JFlex classes and grammar file, use the TokenMakerMaker utility. This
is located in
~/apps/java/TokenMakerMaker-master
and is invoked with

cd ~/apps/java/TokenMakerMaker-master/build/install/tmm
java -jar tokenMakerMaker-3.1.2-SNAPSHOT.jar

Load the syntax file

/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/swing/qdl_syntax.xml

Note that the tool generates ok TokenMaker grammar BUT it is kind of broken in the sense
that the GUI is woefully incomplete and it does not seem to store its own state right,
you must add each token/function/whatever individually or with a file (caveat! if you use
a file, any blank space will be taken as part of the token, so 'abs   ' would be a function
name.) You can only add individual operators (no file import) and functions
must be added individually or all deleted and loaded from a file.
There are complete lists of functions and operators at

/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/swing/all_funcs.txt

/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/swing/all_ops.txt

If there are changes to QDl that need to show up in the GUI text editor, then reload the
qdl_syntax file and make additions etc. The bottom >>generate<< button should be pressed!!
This generates the qdl_syntax.xml file ***AND*** it generates classes in the QDLSyntax.class
in the correct directory. After the tool generates everything,  it
will nicely bring up a RSyntax window and let yo try out the new QDLSyntax.class. This is one
very nice feature of this tool.

In theory we can use ANTLR and pass its output through to a TokenMaker class, but no online attempts
I read to do it seemed to work right and ANTLR's model for issuing tokens is very different
from JFlex. Nobody seems to be able to bridge the gap, so I'm not even going to bother.