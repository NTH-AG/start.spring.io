#!/bin/bash

export APP_NAME=${pom.artifactId}
export PID_FOLDER=.
export LOG_FOLDER=logs
export LOG_FILENAME=${pom.artifactId}.out
export JAVA_HOME=/usr/local/java-1.8
export JAVA_OPTS="-Xms20m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"
export STOP_WAIT_TIME=60

./${pom.artifactId}-${pom.version}.jar "$@"