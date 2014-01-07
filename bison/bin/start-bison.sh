#! /usr/bin/env bash
bin=`dirname "$0"`
bin=`cd "$bin">/dev/null; pwd`

BISON_HOME=/home/chamago/bison_test
JAVA=$JAVA_HOME/bin/java

BISON_OPTS="-Dconf.dir=$BISON_HOME/conf"
BISON_OPTS="$BISON_OPTS -Dbison.service.home=$BISON_HOME"
BISON_PID_DIR=$BISON_HOME/pid
BISON_LOG_DIR=$BISON_HOME/logs
BISON_LOG_PREFIX="bison"

logout=$BISON_LOG_DIR/$BISON_LOG_PREFIX.out  
loglog=$BISON_LOG_DIR/$BISON_LOG_PREFIX.log


if [ "$BISON_PID_DIR" = "" ]; then
  BISON_PID_DIR=/tmp
fi

if [ "$BISON_IDENT_STRING" = "" ]; then
  export BISON_IDENT_STRING="$USER"
fi

pid=$BISON_PID_DIR/bison-$BISON_IDENT_STRING.pid

if [ "$BISON_NICENESS" = "" ]; then
    export BISON_NICENESS=0
fi

mkdir -p "$BISON_PID_DIR"
if [ -f $pid ]; then
   if kill -0 `cat $pid` > /dev/null 2>&1; then
      echo Bison running as process `cat $pid`.  Stop it first.
      exit 1
   fi
fi

echo Starting Bison Server on `hostname`, logging to $logout
echo "`date` Starting BisonServer on `hostname`" >> $loglog
echo "`ulimit -a`" >> $loglog 2>&1
nohup nice -n $BISON_NICENESS "$BISON_HOME"/bin/bison "$@" > "$logout" 2>&1 < /dev/null &
echo $! > $pid