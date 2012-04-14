#!/bin/bash
LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8 MAVEN_OPTS='-Xmx2500m -Xms2225m' mvn package exec:java -Dexec.mainClass=cz.filmtit.core.io.Import -Dmaven.test.skip=true
