package com.neo.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReportExcutor {

	private ThreadPoolExecutor executor;

	private final Logger logger = LoggerFactory.getLogger(ReportExcutor.class);

	public void monitorThreadPoolServices() {
		long task = this.executor.getTaskCount() - this.executor.getCompletedTaskCount();
		logger.info(
				"PoolSize: {}, CorePoolSize: {} Active: {}, Queue: {}, Completed: {}, Task: {}, isShutdown: {}, isTerminated: {}, remaining task: {}",
				this.executor.getPoolSize(), this.executor.getCorePoolSize(), this.executor.getActiveCount(),
				this.executor.getQueue().size(), this.executor.getCompletedTaskCount(), this.executor.getTaskCount(),
				this.executor.isShutdown(), this.executor.isTerminated(), task);
	}

	public void setExecutor(ThreadPoolExecutor executor) {
		this.executor = executor;
	}
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}

}
