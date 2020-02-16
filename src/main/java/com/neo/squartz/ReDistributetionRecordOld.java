package com.neo.squartz;

import java.nio.channels.SocketChannel;
import java.util.Calendar;
import java.util.Map;
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

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ReDistributetionRecordOld extends QuartzJobBean{
	
	private CancelService cancelService;
	
	private PropertiesConfiguration pro;
	
	private ModuleBo ModuleBo;
	
	private ConcurrentHashMap<ModuleBo, SocketChannel> map;
	
	private final Logger logger = LoggerFactory.getLogger(ReDistributetionRecordOld.class);
	
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		String proc = pro.getString("sub.sql.redistribute.record.old");
		String table = pro.getString("table.redistribution.module.disconnect");
		int k=0;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -1);
		k = cancelService.redistributeRecordOld(proc, map, ModuleBo, table,calendar.getTime().getTime());
		System.out.println("========================> phân phối lại các bản ghi đã lâu không xử lý <========================");
		
		if(k==0) {
			logger.error("Retry fileter cancel service");
			for(int i=0;i<50;i++) {
				int m = cancelService.redistributeRecordOld(proc, map, ModuleBo, table,calendar.getTimeInMillis());
				if(m==1) {
					logger.info("Retry fileter cancel service");
					break;
				}else {
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						logger.info("excption {} filtercancel service", ExtractException.exceptionToString(e));
					}
				}
			}
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

	public ModuleBo getModuleBo() {
		return ModuleBo;
	}

	public void setModuleBo(ModuleBo moduleBo) {
		ModuleBo = moduleBo;
	}

	public ConcurrentHashMap<ModuleBo, SocketChannel> getMap() {
		return map;
	}

	public void setMap(ConcurrentHashMap<ModuleBo, SocketChannel> map) {
		this.map = map;
	}

}
