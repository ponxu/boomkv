#!/bin/bash
app_path=`readlink -f $0 | xargs dirname | xargs dirname`
lib_path=$app_path/lib
conf_path=$app_path/conf
bin_path=$app_path/bin

ps -ef | grep com.ponxu.boomkv.core.Bootstrap | grep -v grep | awk '{print $2}' | xargs kill
