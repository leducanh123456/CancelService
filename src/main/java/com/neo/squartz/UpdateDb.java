package com.neo.squartz;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.neo.cancelservie.service.CancelService;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class UpdateDb extends QuartzJobBean{
	
	private CancelService cancelService;
	
	private PropertiesConfiguration pro;
	
	private LinkedBlockingQueue<Map<String, String>> listModulebo;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		String insertLog = pro.getString("sub.sql.insert.log");
		
		String deleteList = pro.getString("sub.sql.delete.list");
		
		String queryUpdate = pro.getString("sub.sql.update.log");
		
		String batchSize = pro.getString("job.batch.size.extend.retry");
		
		cancelService.upDateBatchRenewalRetry(insertLog,deleteList, queryUpdate, listModulebo, batchSize);
		
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
