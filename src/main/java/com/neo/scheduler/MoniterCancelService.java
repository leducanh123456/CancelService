package com.neo.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.neo.App;
import com.neo.cancelservie.service.CancelService;
import com.neo.common.Common;
import com.neo.squartz.GetlListCancelService;
import com.neo.squartz.MultilThread;
import com.neo.utils.Utils;

@Component("moniterCancelService")
public class MoniterCancelService {
	
	public static boolean flag = true;
	
	public static boolean flagCheckServiceCmd = false;
	
	@Autowired
	@Qualifier("executor")
	private ThreadPoolExecutor executor;

	@Autowired
	private CancelService cancelService;

	@Autowired
	@Qualifier("listModulebo")
	private LinkedBlockingQueue<Map<String, String>> listModulebo;
	

	
	@Autowired
	@Qualifier("propertiesConfig")
	private PropertiesConfiguration pro;

	@Autowired
	@Qualifier("serviceCmd")
	private Map<String,Map<String,String>> serviceCmd;
	
	private static int continude = 0;
	
	private final Logger logger = LoggerFactory.getLogger(MoniterCancelService.class);
	
	public void scheduleTaskWithFixedRate() {
	}

	@Scheduled(fixedDelayString = "${fixeddelay.cancel.service.in.milliseconds}")
	public void scheduleTaskWithFixedDelay() {
		
		MoniterCancelService.flag = false;
		//trong lần chạy đâu tiên có thể gây ra lỗi do thằng không phải là master chưa có được service_cmd nên cần kiểm tra
		if(!flagCheckServiceCmd) {
			logger.info("Get service_cmd");
			long startTimecmd = System.nanoTime();
			String procServiceCmd = pro.getString("sub.sql.get.cmd.service");
			Map<String, Map<String, String>>  map = cancelService.getServiceCmds(procServiceCmd);
			Common.updateServiceCmd(serviceCmd, map, logger);
			logger.info("Time run get serive cmd : {} ", Utils.convertTimeUnit(startTimecmd));
			flagCheckServiceCmd=true;
			MoniterCancelService.flag = true;
			return;
		}
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
		Long startTime = System.nanoTime();
		int sizeExcuteConfig = Integer.parseInt(pro.getString("job.number.record.extend.excute"));
		int sizeQueueUpdate = Integer.parseInt(pro.getString("job.update.queue.size.extend.retry"));
		if(continude>0) {
			continude--;
		}else {
			logger.info("run monitor cancel service ");
			if(GetlListCancelService.flag&&executor.getQueue().size()<=sizeExcuteConfig
					&&( sizeQueueUpdate-2*sizeExcuteConfig>(listModulebo.size()))) {
				if(App.IS_IGNORE) {
					return;
				}
				String proc = pro.getString("sub.sql.getlist.cancel.service");
				String module = pro.getString("module.name");
				String numberRecord = pro.getString("job.number.record.extend.excute");
				list = cancelService.getListCancelService(proc, module, Integer.parseInt(numberRecord));
				if(!list.isEmpty()) {
					  for(Map<String, String> element : list) { 
						  Runnable worker = new MultilThread(element,listModulebo,pro,serviceCmd); 
						  executor.submit(worker); 
					  }
					  logger.info("time run monitor cancel service {}", Utils.estimateTime(startTime));
				 }else {
					 continude = 60;
				 }
			}
		}
		MoniterCancelService.flag = true;
	}

	public void scheduleTaskWithInitialDelay() {
	}

	public void scheduleTaskWithCronExpression() {
	}

}
