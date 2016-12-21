#!/bin/bash
app_path=`readlink -f $0 | xargs dirname | xargs dirname`
lib_path=$app_path/lib
conf_path=$app_path/conf
bin_path=$app_path/bin
log_path=$app_path/logs
pid_file=$bin_path/pid

cp=.:$JAVA_HOME/lib/tools.jar:$conf_path
for f in `find $lib_path -name '*.jar'`; do
  cp=$cp:$f
done

opts="-Xmx1024m
-Xms1024m
-XX:+UseConcMarkSweepGC
-XX:MaxTenuringThreshold=10
-verbose:gc
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps
-Xloggc:${log_path}/gc.log
-cp $cp"

envs="-Dlog_path=$log_path
-Dlog4j.configuration=file://$conf_path/log4j.properties
-Duser.dir=$app_path"

exec java $envs $opts com.ponxu.boomkv.core.Bootstrap $@ 2>&1 &
