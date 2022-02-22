#!/bin/sh

pid=`ps ax | grep -i 'femas-admin' |grep java | grep -v grep | awk '{print $1}'`
if [ -z "$pid" ] ; then
        echo "No femas-admin running."
        exit -1;
fi

echo "The femas-admin(${pid}) is running..."

kill ${pid}


echo "Send shutdown request to femas-admin(${pid}) OK"