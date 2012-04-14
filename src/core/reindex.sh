#!/bin/bash
LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8 MAVEN_OPTS='-Xmx2G -Xms2G' mvn package exec:java -Dexec.mainClass=cz.filmtit.core.io.Reindex -Dmaven.test.skip=true
