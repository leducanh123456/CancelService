package com.neo.config;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import com.neo.squartz.ReDistributetionRecordOld;
import com.neo.squartz.UpdateDb;

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
				,cronTriggerFactoryBeanUpdateDb().getObject()
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
		map.put("serviceCmd", getListServiceCmd());
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
		map.put("executor", getThreadPoolExecutorRenewal());
		map.put("listModulebo", getListRenewalRetry());
		map.put("serviceCmd", getListServiceCmd());
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
	
	@Bean(name = "jobReDistributeOldRecord")
	public JobDetailFactoryBean jobDetailFactoryBeansReDistributeOldRecord() {
		JobDetailFactoryBean factory = new JobDetailFactoryBean();
		factory.setJobClass(ReDistributetionRecordOld.class);
		map.put("cancelService", cancelService);
		map.put("pro", pro);
		map.put("ModuleBo", getJobBo());
		map.put("map", getMapJobSocket());
		factory.setJobDataAsMap(map);
		factory.setGroup("reDistributeOldRecord");
		factory.setName("reDistributeOldRecord");
		return factory;
	}

	@Bean(name = "reDistributeOldRecord")
	public CronTriggerFactoryBean cronTriggerFactoryBeanReDistributeOldRecord() {
		CronTriggerFactoryBean stFactory = new CronTriggerFactoryBean();
		stFactory.setJobDetail(jobDetailFactoryBeansReDistributeOldRecord().getObject());
		stFactory.setName("reDistributeOldRecord");
		stFactory.setGroup("reDistributeOldRecord");
		stFactory.setCronExpression(pro.getString("redistribute.old.record.scheduler").trim());
		return stFactory;
	}
	
	@Bean(name = "jobUpdateDb")
	public JobDetailFactoryBean jobDetailFactoryBeanUpdateDb() {
		JobDetailFactoryBean factory = new JobDetailFactoryBean();
		factory.setJobClass(UpdateDb.class);
		map.put("cancelService", cancelService);
		map.put("pro", pro);
		map.put("listModulebo", getListRenewalRetry());
		factory.setJobDataAsMap(map);
		factory.setGroup("updateDb");
		factory.setName("updateDb");
		return factory;
	}

	@Bean(name = "updateDb")
	public CronTriggerFactoryBean cronTriggerFactoryBeanUpdateDb() {
		CronTriggerFactoryBean stFactory = new CronTriggerFactoryBean();
		stFactory.setJobDetail(jobDetailFactoryBeanUpdateDb().getObject());
		stFactory.setName("updateDb");
		stFactory.setGroup("updateDb");
		stFactory.setCronExpression(pro.getString("update.db.cancel.service"));
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
	@Bean("executor")
	@Scope("singleton")
	public ThreadPoolExecutor getThreadPoolExecutorRenewal() {
		int corePoolSize = Integer.parseInt(pro.getString("thread.pool.excutor.extend.rety.core.pool.size"));
		int maxPoolSize = Integer.parseInt(pro.getString("thread.pool.excutor.extend.rety.max.pool.size"));
		int lifeTime = Integer.parseInt(pro.getString("thread.pool.excutor.extend.rety.life.time"));
		ThreadPoolExecutor t = new ThreadPoolExecutor(corePoolSize, maxPoolSize, lifeTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(Integer.parseInt(pro.getString("thread.pool.executor.queue.extend.rety.size").trim())));
        return t;
	}
	
	@Bean("listModulebo")
	@Scope("singleton")
	public LinkedBlockingQueue<Map<String, String>> getListRenewalRetry() {
		LinkedBlockingQueue<Map<String, String>> queue = new LinkedBlockingQueue<Map<String,String>>(Integer.parseInt(pro.getString("job.update.queue.size.extend.retry")));
		return queue;
	}
	
}
