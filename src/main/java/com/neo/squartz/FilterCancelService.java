package com.neo.squartz;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.neo.cancelservie.service.CancelService;
import com.neo.module.bo.ModuleBo;
import com.neo.utils.ExtractException;
import com.neo.utils.Utils;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class FilterCancelService extends QuartzJobBean{
	
	private CancelService cancelService;
	
	private PropertiesConfiguration pro;
	
	private ConcurrentHashMap<ModuleBo, SocketChannel> mapJobSocket;
	
	private Map<String, Map<String, String>> serviceCmd;
	
	private final Logger logger = LoggerFactory.getLogger(FilterCancelService.class);
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		logger.info("Get service_cmd");
		long startTimecmd = System.nanoTime();
		String procServiceCmd = pro.getString("sub.sql.get.cmd.service");
		Map<String, Map<String, String>>  map = cancelService.getServiceCmds(procServiceCmd);
		updateServiceCmd(map);
		logger.info("Time run get serive cmd : {} ", Utils.convertTimeUnit(startTimecmd));
		
		String proc = pro.getString("sub.sql.filter.cancel.service");
		StringBuffer modules = new StringBuffer();
		modules.append(pro.getString("module.name"));
		modules.append(",");
		for (Map.Entry<ModuleBo, SocketChannel> entry : mapJobSocket.entrySet()) {
			modules.append(entry.getKey().getModuleName());
			modules.append(",");
		}
		modules.delete(modules.length()-1, modules.length());
		int k = cancelService.cancelServiceFilter(proc, modules.toString());
		
		if(k==0) {
			logger.error("Retry fileter cancel service");
			for(int i=0;i<50;i++) {
				int m = cancelService.cancelServiceFilter(proc, modules.toString());
				if(m==1) {
					logger.info("Retry fileter cancel service");
					break;
				}else {
					try {
						Thread.sleep(600000);
					} catch (InterruptedException e) {
						logger.info("excption {} filtercancel service", ExtractException.exceptionToString(e));
					}
				}
			}
		}
		
	}
	public void updateServiceCmd(Map<String, Map<String, String>>  map) {
		List<String> listupdate = new ArrayList<String>();
		Set<String> set = map.keySet();
		for(String string : set) {
			if(!serviceCmd.containsKey(string)) {
				listupdate.add(string);
			}
		}
		for(String string : listupdate) {
			serviceCmd.put(string,map.get(string));
		}
		listupdate.clear();
		Set<String> sets = serviceCmd.keySet();
		for(String string : sets) {
			if(!map.containsKey(string)) {
				listupdate.add(string);
			}
		}
		for(String string : listupdate) {
			serviceCmd.remove(string);
		}
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
	public ConcurrentHashMap<ModuleBo, SocketChannel> getMapJobSocket() {
		return mapJobSocket;
	}
	public void setMapJobSocket(ConcurrentHashMap<ModuleBo, SocketChannel> mapJobSocket) {
		this.mapJobSocket = mapJobSocket;
	}
	public Map<String, Map<String, String>> getServiceCmd() {
		return serviceCmd;
	}
	public void setServiceCmd(Map<String, Map<String, String>> serviceCmd) {
		this.serviceCmd = serviceCmd;
	}
	
}
