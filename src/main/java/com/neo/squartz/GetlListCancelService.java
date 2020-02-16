package com.neo.squartz;

import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.neo.cancelservie.service.CancelService;
import com.neo.module.bo.ModuleBo;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GetlListCancelService extends QuartzJobBean{

	private CancelService cancelService;
	
	private PropertiesConfiguration pro;
	
	private ModuleBo ModuleBo;
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		String proc = pro.getString("sub.sql.getlist.cancel.service");
		String module = pro.getString("module.name");
		String numberRecord = pro.getString("job.number.record.extend.excute");
		List<Map<String, String>> list = cancelService.getListCancelService(proc, module, Integer.parseInt(numberRecord));
		System.out.println("size của danh sách là : "+ list.size());
		if(!list.isEmpty()) {
			System.out.println("nó là trống rỗng");
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

}
