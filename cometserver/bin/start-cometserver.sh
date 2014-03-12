#! /usr/bin/env bash
bin=`dirname "$0"`
bin=`cd "$bin">/dev/null; pwd`

COMETSERVER_HOME=/home/chamago/cometserver
JAVA=$JAVA_HOME/bin/java

COMETSERVER_OPTS="-Dconf.dir=$COMETSERVER_HOME/conf"
COMETSERVER_PID_DIR=$COMETSERVER_HOME/pid
COMETSERVER_LOG_DIR=$COMETSERVER_HOME/logs
COMETSERVER_LOG_PREFIX="cometserver"

logout=$COMETSERVER_LOG_DIR/$COMETSERVER_LOG_PREFIX.out  
loglog=$COMETSERVER_LOG_DIR/$COMETSERVER_LOG_PREFIX.log


if [ "$COMETSERVER_PID_DIR" = "" ]; then
  COMETSERVER_PID_DIR=/tmp
fi

if [ "$COMETSERVER_IDENT_STRING" = "" ]; then
  export COMETSERVER_IDENT_STRING="$USER"
fi

pid=$COMETSERVER_PID_DIR/cometserver-$COMETSERVER_IDENT_STRING.pid

if [ "$COMETSERVER_NICENESS" = "" ]; then
    export COMETSERVER_NICENESS=0
fi

mkdir -p "$COMETSERVER_PID_DIR"
if [ -f $pid ]; then
   if kill -0 `cat $pid` > /dev/null 2>&1; then
      echo CometServer running as process `cat $pid`.  Stop it first.
      exit 1
   fi
fi

echo Starting Comet Server on `hostname`, logging to $logout

echo "`date` Starting CometServer on `hostname`" >> $loglog
echo "`ulimit -a`" >> $loglog 2>&1
nohup nice -n $COMETSERVER_NICENESS "$COMETSERVER_HOME"/bin/cometserver "$@" > "$logout" 2>&1 < /dev/null &
echo $! > $pid

