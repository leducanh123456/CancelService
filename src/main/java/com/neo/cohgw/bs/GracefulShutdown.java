package com.neo.cohgw.bs;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.neo.App;

/**
 * @author cimit
 * @Date 22-07-2020
 *
 * GracefulShutdown.java
 */
@Component
public class GracefulShutdown implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {
	
	private final Logger logger = LoggerFactory.getLogger(GracefulShutdown.class);
	
	private static final int TIMEOUT = 30;

	private volatile Connector connector;
	
	@Autowired
	@Qualifier("executor")
	private ThreadPoolExecutor threadPoolExecutor;

	@Override
	public void customize(Connector connector) {
		this.connector = connector;
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		this.connector.pause();
		logger.info("shutdown processing");
		
		Executor executor = this.connector.getProtocolHandler().getExecutor();
		if (executor instanceof ThreadPoolExecutor) {
			try {
				ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
				threadPoolExecutor.shutdown();
				if (!threadPoolExecutor.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
					logger.info("Tomcat thread pool did not shut down gracefully within " + TIMEOUT + " seconds. Proceeding with forceful shutdown");

					threadPoolExecutor.shutdownNow();

					if (!threadPoolExecutor.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
						logger.info("Tomcat thread pool did not terminate");
					}
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
		App.IS_IGNORE = true;
		App.IS_SHUTDOWN = true;
		while(App.IS_SHUTDOWN) {
			if(threadPoolExecutor.getQueue().size()==0) {
				App.IS_SHUTDOWN = false;
			}
		}
		logger.info("shutdown complete");
	}
}
