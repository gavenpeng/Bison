#! /usr/bin/env bash
bin=`dirname "$0"`
bin=`cd "$bin">/dev/null; pwd`

BISON_HOME=/home/xxx/bison_test
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


if [ -f $pid ]; then
   # kill -0 == see if the PID exists 
   if kill -0 `cat $pid` > /dev/null 2>&1; then
      echo -n stopping Bison Server.....
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