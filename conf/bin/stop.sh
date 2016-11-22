#!/bin/bash

cd "$(dirname "$0")"
pidFile=../../RUNNING_PID

kill < $pidFile
rm $pidFile
