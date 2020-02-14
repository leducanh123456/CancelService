package com.neo.config;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.neo.module.bo.ModuleBo;
import com.neo.monitor.NEOMonitorCluster;

@Configuration
public class ConfigSquartz {
	
	SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
	
	@Bean(name = "schedulerFactory")
	public SchedulerFactoryBean schedulerFactoryBean() {
		scheduler.setTriggers(
				
		);
		return scheduler;
		
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
	@Autowired
	@Qualifier("propertiesConfig")
	private PropertiesConfiguration pro;
	
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
	
}
