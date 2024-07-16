JFlex is an Java successor to YACC and a few other parser generators. It
is used in various projects for its simplicity (such as Intellij or the
RSyntaxtTextArea component) but is not nearly as heavy weight as ANTLR.

In theory we could use the actual QDL grammar from ANTLR and pass its output through
to a TokenMaker class, but no online attempts I read to do it seemed to work right
even for real gurus,  and ANTLR's model for issuing tokens is very different
from JFlex to the point that nobody seems to be able to bridge the
gap, except in toy cases, so I'm not even going to bother.

The TokenMakerMaker tool is made to generate JFlex files specifically
for RSyntaxTextArea (RSTA).
RSTA apparently has a number of odd quirks with JFlex to get it to work
right. This meant in practice people could not use RSTA without becoming JFlex
jockeys which was a bad roadblock to using RSTA.
Hence this very specific tool. In short, we need this tool because the
Swing app requires RSTA.

>> In big blocks, load a syntax file in XML, update it, have it generate new classes. <<

NOTE that the tool described here runs under Java 14 or higher only, so switch JVM with

sudo update-alternatives --config java

To generate the JFlex classes and grammar file, use the TokenMakerMaker utility. This
is located in
~/apps/java/TokenMakerMaker-master
and is invoked with

cd ~/apps/java/TokenMakerMaker-master/build/install/tmm
java -jar tokenMakerMaker-3.1.2-SNAPSHOT.jar

(No, there is no release per se of this code.
https://github.com/bobbylight/TokenMakerMaker
Version 3.1.1 from 6/2021 is latest
but has some bad issue(s?) so the snapshot is the stable version.)
Load the syntax file

/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/swing/qdl_syntax.xml

by using file->open and pasting it in the filename box

Note that the tool generates ok TokenMaker grammar BUT it is kind of broken in the sense
that the GUI for this tool is woefully incomplete and it does not
seem to store its own state right.

Summary
*****************************************************************
* Switch to Java 14 or higher.                                  *
* qdl_syntax.xml has current syntax (to be updated)             *
* all_X.txt should have the new keywords added before starting  *
* Pushing generate button generates qdl_syntax.xml and binaries *
* save generated QDLSyntax.* files, qdl_syntax.xml              *
* Switch Java back                                              *
*****************************************************************


Steps
1. Update lists of functions, operators and keywords in
   /home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/swing/all_funcs.txt
   /home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/swing/all_ops.txt
   /home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/swing/all_keywords.txt
   /home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/swing/all_keywords2.txt
   See note below for more

2. Start Flex GUI.
3. Load qdl_syntax.xml
4. load functions, operators and keywords from all_X.txt
5. Hit the generate button to create new qdl_syntax AND binaries. You should
   see a message like

   Writing code to
     "/home/ncsa/dev/ncsa-git/qdl/language/src/main/java/edu/uiuc/ncsa/qdl/gui/flex/QDLSyntax.java"

   in the output window. This means it worked and the GUI is ready for use.

6. The GUI should bring up an RSyntaxTextArea window with the new grammar to test. Do so.
7. The new generated QDLSyntax.* files are in /tmp/edu/uiuc/ncsa/qdl/gui/flex/ and you should
   issue a copy to the main code:
cp /tmp/edu/uiuc/ncsa/qdl/gui/flex/QDLSyntax.* /home/ncsa/dev/ncsa-git/qdl/language/src/main/java/edu/uiuc/ncsa/qdl/gui/flex
    If you do not do this, then no changes will end up in QDL (!!!)
8. Finally, save the updated qdl_syntax.xml file explicitly, just in case.
______
Notes
‾‾‾‾‾‾
1. QDL has lists of these to use. These are in constants.reserved.

   keywords  <--> all_keywords.txt
   keywords2 <--> all_keywords2.txt
   operators <--> all_ops.txt
   functions <--> all_funcs.txt

   E.g. print(constants().reserved.keywords)
   For the operators, yo only need parts of them, so since < and = should
   be on the list, there is no need for <=, >=, ==, === (If these are not
   multiple ascii characters, your editor is showing ligatures!!)

   Keywords2 is actually for unicode operators. The reason is that the operators
   tab ONLY allows for adding one operator at a time -- no file import -- so we
   want to keep these to a minimum.

   Note on booleans: These are not to be included in the keywords, since the flex
   engine supplie these. If these are included, you will get an error about boolean
   literals never being matched (i.e., the built in true/false overrides them).

   NOTE: check each file for errant blanks since e.g. "&& " would be considered
   an operator but not "&&"!!

1. You must add each token/function/whatever individually or with a file
   Any blank space in a file will be taken as part of the token,
   so E.g. 'abs   ' would be a function name.
   You can only add individual operators or whole files. These are atomic

2. DON'T FORGET TO REVERT THE JVM TO THE RIGHT VERSION!!


