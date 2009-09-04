#!/bin/sh
CURRENT_DIR=`pwd`

LINK_DIR=`dirname "$0"`
cd "${LINK_DIR}"
PRG_NAME=`basename "$0"`
PRG="${PRG_NAME}"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  LINK=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$LINK" : '.*/.*' > /dev/null; then
    PRG="${LINK}"
  else
    PRG="`dirname ${PRG}`/${LINK}"
  fi
done

# stop current slf4fx instance if any
PID=`jps | grep slf4fx-server-deps.jar | awk '{printf "%s",$1}'`
[ -n "${PID}" ] && kill ${PID}

# start new slf4fx instance
SLF4FX_HOME=`dirname "$PRG"`
cd "${SLF4FX_HOME}"
java -d64 -Xmx1m -jar "${SLF4FX_HOME}/slf4fx-server-deps.jar" $* &
cd "${CURRENT_DIR}"
