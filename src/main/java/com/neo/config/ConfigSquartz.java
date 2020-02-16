package com.neo.config;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.neo.cancelservie.service.CancelService;
import com.neo.module.bo.ModuleBo;
import com.neo.monitor.NEOMonitorCluster;
import com.neo.squartz.FilterCancelService;
import com.neo.squartz.GetlListCancelService;

@Configuration
public class ConfigSquartz {
	
	@Autowired
	private CancelService cancelService;
	
	@Autowired
	@Qualifier("propertiesConfig")
	private PropertiesConfiguration pro;
	
	private Map<String, Object> map = new HashMap<String, Object>();

	SchedulerFactoryBean scheduler = new SchedulerFactoryBean();

	@Bean(name = "schedulerFactory")
	public SchedulerFactoryBean schedulerFactoryBean() {
		scheduler.setTriggers(
				cronTriggerFactoryBeanGetlistCancelService().getObject()
		);
		return scheduler;
		
	}
	
	@Bean(name = "jobFilterData")
	public JobDetailFactoryBean jobDetailFactoryBeansFilter() {
		JobDetailFactoryBean factory = new JobDetailFactoryBean();
		factory.setJobClass(FilterCancelService.class);
		map.put("cancelService", cancelService);
		map.put("pro", pro);
		map.put("mapJobSocket", getMapJobSocket());
		factory.setJobDataAsMap(map);
		factory.setGroup("filterData");
		factory.setName("filterData");
		return factory;
	}

	@Bean(name = "filterData")
	public CronTriggerFactoryBean cronTriggerFactoryBeanFilter() {
		CronTriggerFactoryBean stFactory = new CronTriggerFactoryBean();
		stFactory.setJobDetail(jobDetailFactoryBeansFilter().getObject());
		stFactory.setName("filterData");
		stFactory.setGroup("filterData");
		stFactory.setCronExpression(pro.getString("filter.data.cancel.service.scheduler").trim());
		return stFactory;
	}
	
	@Bean
	public JobDetailFactoryBean jobDetailFactoryBeansGetlistCancelService() {
		JobDetailFactoryBean factory = new JobDetailFactoryBean();
		factory.setJobClass(GetlListCancelService.class);
		map.put("cancelService", cancelService);
		map.put("pro", pro);
		map.put("ModuleBo", getJobBo());
		factory.setJobDataAsMap(map);
		factory.setGroup("getlist");
		factory.setName("getlist");
		return factory;
	}

	@Bean
	public CronTriggerFactoryBean cronTriggerFactoryBeanGetlistCancelService() {
		CronTriggerFactoryBean stFactory = new CronTriggerFactoryBean();
		stFactory.setJobDetail(jobDetailFactoryBeansGetlistCancelService().getObject());
		stFactory.setName("getlist");
		stFactory.setGroup("getlist");
		stFactory.setCronExpression(pro.getString("getlist.data.cancel.service.scheduler").trim());
		return stFactory;
	}
	
	@Bean("monitor")
	@Scope("singleton")
	public NEOMonitorCluster getCluster() {
		return new NEOMonitorCluster();
	}
	@Bean("listModule")
	@Scope("singleton")
	public List<ModuleBo> getListJob() {
		List<ModuleBo> jobs = Collections.synchronizedList(new ArrayList<ModuleBo>());
		return jobs;
	}
	
	@Bean("mapJobSocket")
	@Scope("singleton")
	public ConcurrentHashMap<ModuleBo, SocketChannel> getMapJobSocket() {
		return new ConcurrentHashMap<ModuleBo, SocketChannel>();
	}
	@Bean("retry")
	@Scope("singleton")
	public ConcurrentHashMap<ModuleBo, SocketChannel> getConcurrentHashMap() {
		return new ConcurrentHashMap<ModuleBo, SocketChannel>();
	}
	
	@Bean("thisModule")
	@Scope("singleton")
	public ModuleBo getJobBo() {
		return new ModuleBo();
	}
	@Bean("serviceCmd")
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public Map<String,Map<String,String>> getListServiceCmd() {
		Map<String, Map<String, String>> serviceCmds = new HashMap<String, Map<String,String>>();
		return serviceCmds;
	}
}
