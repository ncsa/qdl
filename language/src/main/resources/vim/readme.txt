Syntax highlighting for vim. This is very rudimentary.

To install. Create the folder

~/.vim

If needed create the two directories

ftdetect
syntax

which respectively are for File Type detection and syntax. The two files (both named qdl.vim) need
to be put into their respective folders. When you start vim next with a file that has suffix
.qdl or .mdl it should have syntax highlighting enabled.

The syntax highlighting is nor quite complete. The reason is that vim uses regular expressions for
matching and that is simply insufficient (that's why the parser for QDL is wriiten in Antlr, not
a bunch of regex's).