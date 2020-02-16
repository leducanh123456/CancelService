package com.neo.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("cleanMem")
public class CleanMem {
	
	private final Logger logger = LoggerFactory.getLogger(CleanMem.class);
	
	public void scheduleTaskWithFixedRate() {
	}

	@Scheduled(fixedDelayString = "${fixeddelay.clean.mem.in.milliseconds}")
	public void scheduleTaskWithFixedDelay() {
		//logger.info("clean memory......");
		System.gc();
	}

	public void scheduleTaskWithInitialDelay() {
	}

	public void scheduleTaskWithCronExpression() {
	}

}
