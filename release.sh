#/bin/bash

username=tom
projectName=android-heating-app-api
projectPath="/home/tom/server/$projectName"
server=192.168.1.7 
port=22
startupScript="$projectName-1.0/bin/$projectName"
shutdownScript="$projectName-1.0/conf/bin/stop.sh"
zipFile="$projectName-1.0.zip"
app="target/universal/$zipFile"
files="$app"

echo "building project"
sbt dist 

echo "shutting down $projectPath"
ssh -p $port -n ${username}@${server} "cd $projectPath; ./$shutdownScript;"
echo "Rsyncing with ${username}@${server}:${projectPath}"
rsync -avz --delete --rsh="ssh -p $port"  $files ${username}@${server}:${projectPath}  || exit 1
echo ""
echo "Rsynced with ${username}@${server}:${projectPath}"
echo "Starting $projectPath"
ssh -p $port -n ${username}@${server} "cd $projectPath; rm -Rf $projectName; unzip -ou $zipFile;  ./$startupScript &  > /dev/null"
