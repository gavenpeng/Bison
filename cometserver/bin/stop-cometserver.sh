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


if [ -f $pid ]; then
   # kill -0 == see if the PID exists 
   if kill -0 `cat $pid` > /dev/null 2>&1; then
      echo -n stopping CometServer.....
      echo "`date` Terminating $command" >> $loglog
      kill `cat $pid` > /dev/null 2>&1
      while kill -0 `cat $pid` > /dev/null 2>&1; do
        echo -n "."
        sleep 1;
      done
      rm $pid
      echo
   else
     retval=$?
     echo no $command to stop because kill -0 of pid `cat $pid` failed with status $retval
   fi
else
   echo no $command to stop because no pid file $pid
fi

