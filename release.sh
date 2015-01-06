#!/bin/bash

username=tom
projectPath=/home/tom/Server/HomeAutomationRest
server=tomliddle.asuscomm.com
port=40
startupScript=start.sh
shutdownScript=stop.sh

echo "shutting down $projectPath"
ssh -p $port -n ${username}@${server} "cd $projectPath; ./$shutdownScript;"
echo "Rsyncing with ${username}@${server}:${projectPath}"
rsync -avz --delete --rsh="ssh -p $port"  build/libs/ ${username}@${server}:${projectPath}  || exit 1
echo ""
echo "Rsynced with ${username}@${server}:${projectPath}"
echo "Starting $projectPath"
ssh -p $port -n ${username}@${server} "cd $projectPath; ./$startupScript > /dev/null"
