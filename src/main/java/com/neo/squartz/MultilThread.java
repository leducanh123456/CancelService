package com.neo.squartz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.cohgw.bs.HttpClient;
import com.neo.cohgw.bs.NEORequest;
import com.neo.cohgw.bs.NEOResponse;
import com.neo.common.StringRandom;
import com.neo.utils.ExtractException;

public class MultilThread implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(MultilThread.class);

	private Object tmp;

	private NEOResponse neoResponse;

	private NEORequest neoRequest;

	private LinkedBlockingQueue<Map<String, String>> queueRenewalRetry;

	private PropertiesConfiguration propertiesConfiguration;

	private Map<String, Map<String, String>> serviceCmds;

	public MultilThread( Object tmp,
			LinkedBlockingQueue<Map<String, String>> queueRenewalRetry, PropertiesConfiguration propertiesConfiguration,
			Map<String, Map<String, String>> serviceCmds) {
		this.tmp = tmp;
		this.queueRenewalRetry = queueRenewalRetry;
		this.propertiesConfiguration = propertiesConfiguration;
		this.serviceCmds = serviceCmds;
	}

	@Override
	public void run() {
		try {
			if (tmp instanceof Map) {
				appExtend();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void appExtend() {
		
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Long startTime = System.currentTimeMillis();
			
			@SuppressWarnings("unchecked")
			Map<String, String> parameter = (Map<String, String>) tmp;

			String medatataRenewal = propertiesConfiguration.getString("sub.sql.metadata.extend.retry").trim();
			String arrayMedata[] = medatataRenewal.split("\\,");
			Map<String, String> callApi = new HashMap<String, String>();
			
			String channel = propertiesConfiguration.getString("job.extend.retry.channel");
			callApi.put("CHANNEL", channel);
			parameter.put("CHANNEL", channel);
			
			for (int i = 0; i < arrayMedata.length; i++) {
				callApi.put(arrayMedata[i].toUpperCase(), parameter.get(arrayMedata[i].toUpperCase()).toUpperCase());
			}
			String uniqueID = StringRandom.getRandomCode("", 18);
			StringBuilder string = new StringBuilder();
			string.append(parameter.get("SERVICE_ID"));
			string.append("_");
			string.append(parameter.get("PKG_ID"));
			string.append("_");
			string.append("2");
			if (serviceCmds.get(string.toString()) != null) {
				Map<String, String> map = serviceCmds.get(string.toString());
				callApi.put("SERVICE_CMD", map.get("CMD"));
				callApi.put("SESSION", uniqueID);
				callApi.put("CLEAN_SUB", "1");
				
				neoRequest = new NEORequest();
				neoRequest.addMapParameter(callApi);
				
				String paramUrl=null;
				try {
					paramUrl = objectMapper.writeValueAsString(neoRequest);
				} catch (JsonProcessingException e) {
					logger.error("excption {} MutilThreadMt", ExtractException.exceptionToString(e));
				}
				String result =null;
				try {
					result = HttpClient.sendJson(paramUrl,propertiesConfiguration.getString("extend.api"));
				} catch (IOException e) {
					logger.error("excption {} MutilThreadMt", ExtractException.exceptionToString(e));
				}
				
				neoResponse = objectMapper.readValue(result, NEOResponse.class);
				String status = neoResponse.getStatus();
				String statusMessage =neoResponse.getStatusMessage();
				
				if (status!=null) {
					parameter.replace("STATE", status);
				} else {
					parameter.replace("STATE", "-1");
				}
				parameter.put("SERVICE_CMD", map.get("CMD"));
				parameter.put("SESSION_ID", uniqueID);
				logger.info(" session : {} :request : {}",uniqueID, neoRequest.toString());
				logger.info(" session : {} :response : {}",uniqueID, result);
				parameter.put("STATUS", status);
				parameter.put("STATUS_MESSAGE", statusMessage);
			} else {
				neoRequest = new NEORequest();
				neoRequest.addMapParameter(callApi);
				parameter.put("STATUS", "");
				logger.info(" session : {} :request : {}",uniqueID, neoRequest.toString());
				parameter.replace("STATE", "-1");
				parameter.put("SERVICE_CMD", "");
			}
			Long stopTime = System.currentTimeMillis();
			parameter.put("SPEND_TIME", "" + (stopTime - startTime));
			queueRenewalRetry.add(parameter);
			
		} catch (Exception e) {
			logger.info("excption {}",e);
		}
	}
}