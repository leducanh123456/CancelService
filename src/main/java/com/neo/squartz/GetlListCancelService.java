package com.neo.squartz;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.neo.App;
import com.neo.cancelservie.service.CancelService;
import com.neo.common.Common;
import com.neo.module.bo.ModuleBo;
import com.neo.scheduler.MoniterCancelService;
import com.neo.utils.Utils;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GetlListCancelService extends QuartzJobBean{
	
	public static Calendar lastupdateCmd = Calendar.getInstance();

	private CancelService cancelService;
	
	private PropertiesConfiguration pro;
	
	private ModuleBo ModuleBo;
	
	private ThreadPoolExecutor executor;
	
	private LinkedBlockingQueue<Map<String, String>> listModulebo;
	
	private Map<String, Map<String, String>> serviceCmd;
	
	public static boolean flag = true;
	
	private final Logger logger = LoggerFactory.getLogger(GetlListCancelService.class);
	
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		flag = false;
		if(!MoniterCancelService.flagCheckServiceCmd) {
			logger.info("Get service_cmd");
			long startTimecmd = System.nanoTime();
			String procServiceCmd = pro.getString("sub.sql.get.cmd.service");
			Map<String, Map<String, String>>  map = cancelService.getServiceCmds(procServiceCmd);
			Common.updateServiceCmd(serviceCmd, map, logger);
			logger.info("Time run get serive cmd : {} ", Utils.convertTimeUnit(startTimecmd));
			MoniterCancelService.flagCheckServiceCmd=true;
			flag = false;
			return;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -5);
		if(ModuleBo.getIsMaster()!=1 && lastupdateCmd.before(calendar)) {
			String procServiceCmd = pro.getString("sub.sql.get.cmd.service");
			Map<String, Map<String, String>>  map = cancelService.getServiceCmds(procServiceCmd);
			logger.info("update service_cmd in module not master");
			Common.updateServiceCmd(serviceCmd, map, logger);
			lastupdateCmd = Calendar.getInstance();
		}
		int sizeExcuteConfig = Integer.parseInt(pro.getString("job.number.record.extend.excute"));
		int sizeQueueUpdate = Integer.parseInt(pro.getString("job.update.queue.size.extend.retry"));
		if(MoniterCancelService.flag&&executor.getQueue().size()<=sizeExcuteConfig
				&&( sizeQueueUpdate-2*sizeExcuteConfig>(listModulebo.size()))) {
			if(App.IS_IGNORE) {
				return;
			}
			String proc = pro.getString("sub.sql.getlist.cancel.service");
			String module = pro.getString("module.name");
			String numberRecord = pro.getString("job.number.record.extend.excute");
			List<Map<String, String>> list = cancelService.getListCancelService(proc, module, Integer.parseInt(numberRecord));
			if(!list.isEmpty()) {
				  for(Map<String, String> element : list) { 
					  Runnable worker = new MultilThread(element,listModulebo,pro,serviceCmd); 
					  executor.submit(worker); 
				  }
			  }
		}
		flag = true;
		
	}

	public CancelService getCancelService() {
		return cancelService;
	}

	public void setCancelService(CancelService cancelService) {
		this.cancelService = cancelService;
	}

	public PropertiesConfiguration getPro() {
		return pro;
	}

	public void setPro(PropertiesConfiguration pro) {
		this.pro = pro;
	}

	public ModuleBo getModuleBo() {
		return ModuleBo;
	}

	public void setModuleBo(ModuleBo moduleBo) {
		ModuleBo = moduleBo;
	}

	public ThreadPoolExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(ThreadPoolExecutor executor) {
		this.executor = executor;
	}

	public LinkedBlockingQueue<Map<String, String>> getListModulebo() {
		return listModulebo;
	}

	public void setListModulebo(LinkedBlockingQueue<Map<String, String>> listModulebo) {
		this.listModulebo = listModulebo;
	}

	public Map<String, Map<String, String>> getServiceCmd() {
		return serviceCmd;
	}

	public void setServiceCmd(Map<String, Map<String, String>> serviceCmd) {
		this.serviceCmd = serviceCmd;
	}
	
}
