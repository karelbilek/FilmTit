install:
	mvn clean install -Dmaven.test.skip=true

import:
	LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8 MAVEN_OPTS='-Xmx2500m -Xms2225m' mvn exec:java -Dexec.mainClass=cz.filmtit.core.io.Import -pl core

reindex:
	LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8 MAVEN_OPTS='-Xmx2500m -Xms2225m' mvn exec:java -Dexec.mainClass=cz.filmtit.core.io.Reindex -pl core

.PHONY: import
