#!/bin/bash

cd `dirname $0`
lockfile=lock/lock

if [ ! -f $lockfile ]; then
	echo "Warning - no lock file - assuming not running"
	exit 1
fi

pid=`cat $lockfile`
kill -0 $pid > /dev/null 2>&1

if [ $? -ne 0 ]; then
   echo "Warning pid \"$pid\" doesn't correspond to a running bot process"
   exit 1
fi

echo "Killing process $pid"
i=0
while [ $i -lt 5 ]; do
	kill $pid
	sleep 5
	kill -0 $pid > /dev/null 2>&1
	if [[ $? -ne 0 ]]; then
		echo "shut down"
		rm -f $lockfile
		exit 0
 	fi
	i=$(( $i + 1 ))
done

echo "Has not shutdown gracefully, killing with -9"
kill -9 $pid
sleep 5
rm -f $lockfile
