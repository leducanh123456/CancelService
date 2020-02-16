package com.neo.squartz;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.neo.cancelservie.service.CancelService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class UpdateDb extends QuartzJobBean{
	
	private CancelService cancelService;
	
	private PropertiesConfiguration pro;
	
	private LinkedBlockingQueue<Map<String, String>> listModulebo;
	
	private final Logger logger = LoggerFactory.getLogger(UpdateDb.class);

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		logger.info("update Data base ==============================================>>>>>");
		String proc = pro.getString("sub.sql.move.to.log");
		
		String queryUpdate = pro.getString("sub.sql.update.log");
		
		String batchSize = pro.getString("job.batch.size.extend.retry");
		
		cancelService.upDateBatchRenewalRetry(proc, queryUpdate, listModulebo, batchSize);
		
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

	public LinkedBlockingQueue<Map<String, String>> getListModulebo() {
		return listModulebo;
	}

	public void setListModulebo(LinkedBlockingQueue<Map<String, String>> listModulebo) {
		this.listModulebo = listModulebo;
	}

}
