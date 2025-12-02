#!/bin/bash

JAR=dasan-0.0.1-SNAPSHOT.jar
LOG=/home/ubuntu/dasan/dasan.log

nohup java -Dspring.profiles.active=prod -jar $JAR > $LOG 2>&1 &
