#!/bin/bash

# Run this so that GUI compiles for you; I have already run it on the filmtit server.

wget http://lib-gwt-file.googlecode.com/files/lib-gwt-file-0.3.jar
mvn install:install-file -DgroupId=org.vectomatic -DartifactId=lib-gwt-file -Dversion=3.0.0 -Dpackaging=jar -Dfile=lib-gwt-file-0.3.jar