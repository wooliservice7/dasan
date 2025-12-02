#!/bin/bash

JAR=dasan-0.0.1-SNAPSHOT.jar
LOG=/home/ubuntu/dasan/dasan.log

nohup java -Xms1g -Xmx1g -Duser.timezone=Asia/Seoul -Dspring.profiles.active=prod -jar $JAR > $LOG 2>&1 &
