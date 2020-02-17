package com.neo.squartz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.neo.cancelservie.service.CancelService;
import com.neo.utils.Utils;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class UpdateCmdService extends QuartzJobBean {

	private CancelService cancelService;

	private Map<String, Map<String, String>> serviceCmds;

	private PropertiesConfiguration pro;

	private final Logger loggerRun = LoggerFactory.getLogger(UpdateCmdService.class);

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		// TODO Auto-generated method stub
		loggerRun.info("Get service_cmd");
		long startTimecmd = System.nanoTime();
		String procServiceCmd = pro.getString("sub.sql.get.cmd.service");
		Map<String, Map<String, String>> map = cancelService.getServiceCmds(procServiceCmd);
		loggerRun.info("Time run get serive cmd : {} ", Utils.convertTimeUnit(startTimecmd));
		updateServiceCmd(map);

	}

	public void updateServiceCmd(Map<String, Map<String, String>> map) {
		List<String> listupdate = new ArrayList<String>();
		Set<String> set = map.keySet();
		for (String string : set) {
			if (!serviceCmds.containsKey(string)) {
				listupdate.add(string);
			}
		}
		for (String string : listupdate) {
			serviceCmds.put(string, map.get(string));
		}
		listupdate.clear();
		Set<String> sets = serviceCmds.keySet();
		for (String string : sets) {
			if (!map.containsKey(string)) {
				listupdate.add(string);
			}
		}
		for (String string : listupdate) {
			serviceCmds.remove(string);
		}
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

	public Logger getLoggerRun() {
		return loggerRun;
	}

}