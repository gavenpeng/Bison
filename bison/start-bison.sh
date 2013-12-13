#! /usr/bin/env bash
bin=`dirname "$0"`
bin=`cd "$bin">/dev/null; pwd`

BISON_HOME=/home/chamago/appdata/bison
JAVA=$JAVA_HOME/bin/java

BISON_OPTS="$BISON_OPTS -Dconf.dir=$BISON_HOME/conf"
BISON_OPTS="$BISON_OPTS -Dbison.service.home=$BISON_HOME"

CLASSPATH=$JAVA_HOME/lib/tools.jar
CLASSPATH=${CLASSPATH}:$BISON_HOME/bison-server-0.0.1-SNAPSHOT.jar

for f in $BISON_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

CLASS='com.mina.bison.server.BisonServer'

exec "$JAVA" -XX:OnOutOfMemoryError="kill -9 %p" $BISON_OPTS -classpath "$CLASSPATH" $CLASS "$@"
