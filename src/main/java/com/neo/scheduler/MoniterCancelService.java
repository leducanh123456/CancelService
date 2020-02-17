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

import com.neo.cancelservie.service.CancelService;
import com.neo.squartz.GetlListCancelService;
import com.neo.squartz.MultilThread;
import com.neo.utils.Utils;

@Component("moniterCancelService")
public class MoniterCancelService {
	
	public static boolean flag = true;
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
