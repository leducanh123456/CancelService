#/bin/sh

#
# Service NEIFGW
# OS User:      appgw
# start/stop/clean/check script for service NEIFGW
# author 	: CanhMH
# Date 		: 07/11/2019
# ident "%Z%%M% %I% %E SMI"
MAX_COUNT=600
SERVER_HOST=10.54.146.220
SERVER_PORT=8786
JAVA_MAIN=com.neo.App
MODULE_NAME="Job Cancel Service"
JAVA_OPS="-d64 -Xms64M -Xss256K -Xmx2048M -XX:MaxMetaspaceSize=2048M -Dfile.encoding=UTF-8 -Doracle.jdbc.defaultNChar=true -Dserver.host=${SERVER_HOST} -Dserver.port=${SERVER_PORT}"

port2PID()
{	
	AWK_CONDITIONS=''
	for var in "$@"
	do
		AWK_CONDITIONS="$AWK_CONDITIONS/${var}/&&"
	done
	set -- "${newparams[@]}"
	len=`expr ${#AWK_CONDITIONS} - 2`
	AWK_CONDITIONS=$(echo $AWK_CONDITIONS | cut -c0-$len)
	
	pid=$(netstat -aun | awk $AWK_CONDITIONS | grep LISTEN | awk '{print $4}' | grep -v grep | head -n1)
	echo $pid
}

do_start()
{	
	pid=$(port2PID ${SERVER_HOST} ${SERVER_PORT})
	if [[ ${#pid} -gt 0 ]]; then
		echo "$MODULE_NAME is Running on pid=" $pid
	else
			count=0
			nohup java $JAVA_OPS -classpath "libs/*" $JAVA_MAIN > logs/server.log  2>&1 &
			
			echo "Execute: nohup java $JAVA_OPS -classpath 'libs/*' $JAVA_MAIN > logs/server.log  2>&1 &"
			while [[ ( -z $(grep "Server Started" "logs/server.log") ) && ( $count -lt $MAX_COUNT ) ]]
			do 
				sleep 1; 
				count=`expr $count + 1`
			done 
			echo "Start $MODULE_NAME count=$count"
	fi
}

do_stop()
{	
	pid=$(port2PID ${SERVER_HOST} ${SERVER_PORT})
	if [[ ${#pid} -gt 0 ]]; then
		kill -9 $pid
		echo "$MODULE_NAME stopped"
	else
		echo "$MODULE_NAME stopped"
	fi
}

do_status()
{	
	pid=$(port2PID ${SERVER_HOST} ${SERVER_PORT})
	if [[ ${#pid} -gt 0 ]]; then
		echo "$MODULE_NAME is running pid=$pid"
	else
		echo "$MODULE_NAME stopped"
	fi
	
}

#####################################################################
#	Service 
#####################################################################
case "$1" in
	'start')
		logger 1 "Service start"
		do_start
		;;
	'stop')
		logger 1 "Service stop"
		do_stop
		;;
	'check')
		logger 1 "Service check"
		do_status
		;;
	'status')
		logger 1 "Service status"
		do_status
		;;
	'refresh')
		logger 1 "Service refresh"
		do_stop
		do_start
		;;
	*)
		echo $"Usage: $0 {start|stop|check|status|refresh}"
		exit $SMF_EXIT_ERR_CONFIG
	 ;;
esac