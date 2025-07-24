# QDL
The QDL programming language

 QDL (pronounced "quiddle") is a functional programming langugage that 
 operates on aggregates (called stems) and also has
 programming control structures. As such, it is more of an algorithmic
 notation that happens to run on computers.

# Installing QDL

You can get the [qdl installer jar](https://github.com/ncsa/qdl/releases/latest) directly from GitHub.
To use, simply invoke
```
java -jar qdl-installer.jar install -all -dir $QDL_HOME
```
where $QDL_HOME is the directory you wish it installed to. (We suggest /opt/qdl).
A useful command line option is `--help` which will print out help and
instructions. You should set `$QDL_HOME` in your environment and
add `$QDL_HOME/qdl/bin` to your `PATH` variable to be able to run qdl directly.

If you are upgrading to another version of QDL, issue
```
java -jar qdl-installer.jar upgrade -all -dir $QDL_HOME
```

If you are just using QDL, the rest of this document may be ignored.

-------

# Using QDL as a dependency.

The base QDL jar is in Maven and should be referenced as
```
<dependency>
  <groupId>org.qdl_lang</groupId>
  <artifactId>language</artifactId>
  <version>1.6.2</version>
</dependency>
```
(Or whatever version you want.)


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

By building it, I mean a complete recompile and the creation of artifacts suitable for release.
Get the sources from [GitHub](https://github.com/ncsa/qdl) and clone to 'NCSA_DEV_INPUT'.
You should then be able to build the system by issuing 

`./build.sh`

from the top-level of the checkout. Note that this will reference the variables
you have set, so be sure they are correct!

## Output

The output will be in `NCSA_DEV_OUTPUT/qdl` and will consist of the new directory, `qdl`
which contains the distribution _and_ the installer, qdl-installer.jar. 

Note that building against a release is probably the best way. You can check out 
from the main branch and try to build against the snapshot release, but you should
be prepared to get other dependencies (such as the NCSA security library) and
build those.
