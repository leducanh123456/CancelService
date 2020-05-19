package com.neo.squartz;

import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.neo.cancelservie.service.CancelService;
import com.neo.common.Common;
import com.neo.utils.Utils;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class UpdateCmdService extends QuartzJobBean {

	private CancelService cancelService;

	private Map<String, Map<String, String>> serviceCmds;

	private PropertiesConfiguration pro;

	private final Logger logger = LoggerFactory.getLogger(UpdateCmdService.class);

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		logger.info("Get service_cmd");
		long startTimecmd = System.nanoTime();
		String procServiceCmd = pro.getString("sub.sql.get.cmd.service");
		Map<String, Map<String, String>> map = cancelService.getServiceCmds(procServiceCmd);
		logger.info("Time run get serive cmd : {} ", Utils.convertTimeUnit(startTimecmd));
		Common.updateServiceCmd(serviceCmds, map, logger);

	}

	public CancelService getCancelService() {
		return cancelService;
	}

	public void setCancelService(CancelService cancelService) {
		this.cancelService = cancelService;
	}

	public Map<String, Map<String, String>> getServiceCmds() {
		return serviceCmds;
	}

	public void setServiceCmds(Map<String, Map<String, String>> serviceCmds) {
		this.serviceCmds = serviceCmds;
	}

	public PropertiesConfiguration getPro() {
		return pro;
	}

	public void setPro(PropertiesConfiguration pro) {
		this.pro = pro;
	}

}