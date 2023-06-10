#!/bin/bash

until curl -s -f -o /dev/null "http://namenode:9870/"
do
  sleep 2
done

hdfs dfsadmin -safemode leave
hdfs dfs -mkdir /spark-logs
$SPARK_HOME/sbin/start-history-server.sh