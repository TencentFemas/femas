#!/bin/bash
set -e

# femas-admin root dir
BASE_DIR=$(dirname $(dirname $(realpath "$0")))
[ -z "$HOME" ] && HOME=/root

# 判断参数类型
if [ -n "$2" ]; then
  DB_TYPE=$1
  JAVA_HOME=$2
else
  [ -d $1 ] && JAVA_HOME=$1 || DB_TYPE=$1
fi

# 获取java安装目录
FindJava() {
  [ -d "$JAVA_HOME" ] && JAVA_PATH=`echo "find '$JAVA_HOME' -name "java" -type f | head -n 1" | sh`
  [ -e "$JAVA_PATH" ] && return || JAVA_PATH=$(which java)
  [ -e "$JAVA_PATH" ] && return || JAVA_PATH=$(find $HOME -name "java" -type f | head -n 1)
  [ -e "$JAVA_PATH" ] && return || echo "JAVA Not Found."; exit 0
}
FindJava

JAVA_OPT="$JAVA_OPT -server -Xms2g -Xmx2g -Xmn1g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m"
JAVA_OPT="$JAVA_OPT -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$BASE_DIR/femas_java_heapdump.hprof"
JAVA_OPT="$JAVA_OPT -XX:-UseLargePages"
JAVA_OPT="$JAVA_OPT -Dfemas.home=$BASE_DIR"

JAVA_MAJOR_VERSION=$($JAVA_PATH -version 2>&1 | sed -E -n 's/.* version "([0-9]*).*$/\1/p')
if [[ "$JAVA_MAJOR_VERSION" -ge "9" ]] ; then
  JAVA_OPT="$JAVA_OPT -Xlog:gc*:file=$BASE_DIR/logs/femas_gc.log:time,tags:filecount=10,filesize=102400"
else
  JAVA_OPT="$JAVA_OPT -Xloggc:$BASE_DIR/logs/femas_gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
fi

# 选择数据源
if [ -n "$DB_TYPE" ] && [ "external" == "$DB_TYPE" ]; then
  JAVA_OPT="$JAVA_OPT -DdbType=external"
fi

JAVA_OPT="$JAVA_OPT -Dspring.config.location=$BASE_DIR/conf/bootstrap.yaml -jar $BASE_DIR/femas-admin.jar"
JAVA_OPT="$JAVA_OPT --logging.config=$BASE_DIR/conf/logback-spring.xml"

# 创建日志目录
if [ ! -d "$BASE_DIR/logs" ]; then
  mkdir "$BASE_DIR/logs"
fi

# 启动 femas-admin
start(){
  echo "nohup $JAVA_PATH $JAVA_OPT > $BASE_DIR/logs/femas_admin.log 2>&1 &"
  echo "nohup '$JAVA_PATH' $JAVA_OPT >> $BASE_DIR/logs/femas_admin.log 2>&1 &" | sh
  echo "femas-admin is starting，logFile is in $BASE_DIR/logs/femas_admin.log"
}
start