# QDL
The QDL programming language

 QDL (pronounced "quiddle") is a functional programming langugage that 
 operates on aggregates (called stems) and has
 programming control structures. As such, it is more of an algorithmic
 notation that happens to run on computers.

# Using QDL as a dependency.

The base QDL jar is in Maven and should be referenced as
```
<dependency>
  <groupId>edu.uiuc.ncsa.qdl</groupId>
  <artifactId>language</artifactId>
  <version>1.5-QDL-SNAPSHOT</version>
</dependency>
```
(Or whatever version you want.)

# Installing QDL

You can get the [qdl installer jar](https://github.com/ncsa/qdl/releases/latest) directly from GitHub.
To use, simply invoke 

'java -jar qdl-installer.jar'

A useful supported command line option is `--help` which will print out help and 
instructions. Typically this will put the entire distribution in a directory you
specify which should be set to the environment variable `QDL_HOME`. You can also
add `$QDL_HOME/qdl/bin` to your `PATH` variable to be able to run qdl directly. 


# Building

Should you want/need to build QDL directly, you should follow these instructions. generally
though that is not required and the latest version should be all you need.

## System Requirements

* Java 11
* Maven 3.6 or higher

## Environment Variables
 
* `NCSA_DEV_INPUT`  = The root directory for sources
* `NCSA_DEV_OUTPUT` = The target directory for all artifacts
* `NCSA_CONFIG_ROOT` = Location of sensitive configuration

The `NCSA_CONFIG_ROOT` contains files with information that should not be
committed to GitHub such as your local database password etc. 

# How to build it

Get the sources from [GitHub](https://github.com/ncsa/qdl) and clone to 'NCSA_DEV_INPUT'.
You should then be able to build the system by issuing 

`./build.sh`

from the top-level of the checkout. Note that this will reference the variables
you have set, so be sure they are correct!

## Output

The output will be in `NCSA_DEV_OUTPUT/qdl` and will consist of the new directory, `qdl`
which contains the distribution _and_ the installer, qdl-installer.jar. You may
just distribute the QDL installer (don't forget it has an upgrade option 
for an existing install, invoke it as 

`java -jar qdl-installer.sh --help`

for more information.)


Note that building against a release is probably the best way. You can check out 
from the main branch and try to build against the snapshot release, but you should
be prepared to get other dependencies (such as the NCSA security library) and
build those.

# Updating the language

By updating the language, we mean a change to the parser itself, such as the addition
of new operators or behavior. It is _highly_ unlikely this needs to be done, however,
we will describe how to do it. 

QDL has the binaries for the parser already in place, so to update
the langauge itself, you would need to regenerate all of these. 
Fortunately it is pretty easy with the scipt supplied, but is not for
the faint of heart unless you have a lot  of experience with lexers and grammars.

In the unlikely event you need to update the language (such as adding a new operator)
you will need to install ANTLR 4.9.3. Since ANTLR is very picky about versions, there are no promises
that other versions will build right. Sorry. In any case, once you are ready to 
build go to `$NCSA_DEV_INPUT/src/main/antlr4' and issue

`build.sh`

which will create all of the files you need in the right place. You should then
rebuild QDL from the ground up.

## Updating the ini file parser

This is very similar indeed to updating the language, but the  files live
in `$NCSA_DEV_INPUT/src/main/antl4/iniFile`

# Deploying to Sonatype

This is done at the command line. Be sure that you have registered with Sonatype
as an administrator and have uploaded your signing keys. You should enable GPG
signing in `$DEV_NCSA_INPUT/qdl/pom.xml` and should comment out the website module.
Once that is done, issue

`mvn clean install deploy`

and this will install it locally plus upload it to Sonatype. You will still have
to log in there to close then release it.
