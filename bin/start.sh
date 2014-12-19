#!/bin/bash

cd `dirname $0`
mkdir -p lock
lockfile=lock/lock



(
	flock -n 9 || {
		echo "HA already running, terminating"
		exit 1
	}
	# ... commands executed under lock ...
	nohup java -cp HomeAutomationRest.jar:* com.tomliddle.JettyLauncher 2>&1 &
	echo $! > $lockfile
) 9>>$lockfile
