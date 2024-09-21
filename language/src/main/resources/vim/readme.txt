Syntax highlighting for vim. This is very rudimentary.

To install. The Right Way (assuming there may be more syntax files from
a third part at some point) is, if needed,  to create the folder

~/.vim

and if needed create the two directories

ftdetect
syntax

Then if unix, add links to the syntax files

ln -s $QDL_HOME/etc/vim/ftdetect/qdl.vim ~/.vim/ftdetect/qdl.vim
ln -s $QDL_HOME/etc/vim/syntax/qdl.vim ~/.vim/syntax/qdl.vim

which respectively are for File Type detection and syntax. The two files (both named qdl.vim) need
to be put in/linked to their respective folders. When you start vim next with a file that has suffix
.qdl or .mdl it should have syntax highlighting enabled.

The really quick way (assuming you don't have other customized syntax files you are
managing) is to just point to the QDL syntax files in toto:

ln -s $QDL_HOME/etc/vim/ ~/.vim

Note that the plus with links is that whenever QDL is updated, syntax files are updated with no
intervention from you. If you have copies of these files, you will have to update them whenever
they are updated in the QDL distribution.

The syntax highlighting is not quite complete. The reason is that vim uses regular expressions for
matching and that is simply insufficient (that's why the parser for QDL is wriiten in Antlr, not
a bunch of regex's).