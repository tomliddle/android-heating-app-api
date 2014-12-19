#!/bin/bash

username=tom
projectPath=/home/tom/Projects/HomeAutomationRest
server=tomliddle.asuscomm.com
port=40

echo "Rsyncing with ${username}@${server}:${projectPath}"
rsync -avz --delete --exclude out --exclude build --rsh="ssh -p $port"  . ${username}@${server}:${projectPath}  || exit 1
echo ""
echo "Rsynced with ${username}@${server}:${projectPath}"
