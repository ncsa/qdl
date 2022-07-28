This directory contains the grammar and build script for the QDL ini file parser.
To use:

 * switch to this directory (Antlr does not work from another one)
 * Set any paths in build.sh you need for your system
 * run build.sh (this repopulates the ini_generated directory)
 * re-import the package ini_generated
 * rebuild the entire project

Note that ini_generated should, of course, never be touched directly.