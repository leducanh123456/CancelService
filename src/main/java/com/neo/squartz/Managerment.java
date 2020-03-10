package com.neo.squartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.neo.config.ReportExcutor;
import com.neo.config.ResourceManagement;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class Managerment extends QuartzJobBean {

	private ResourceManagement resourceManagement;

	private ReportExcutor reportExcutor;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		resourceManagement.reportResource();
		reportExcutor.monitorThreadPoolServices();

	}

	public ResourceManagement getResourceManagement() {
		return resourceManagement;
	}

	public void setResourceManagement(ResourceManagement resourceManagement) {
		this.resourceManagement = resourceManagement;
	}

	public ReportExcutor getReportExcutor() {
		return reportExcutor;
	}

	public void setReportExcutor(ReportExcutor reportExcutor) {
		this.reportExcutor = reportExcutor;
	}

}
