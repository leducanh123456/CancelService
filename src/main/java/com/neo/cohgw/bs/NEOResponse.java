package com.neo.cohgw.bs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * @author cimit
 * @Date 04-07-2020
 *
 *       NEOResponse.java
 */
@JacksonXmlRootElement(localName = "res", namespace = VASNamespace.NAMESPACE)
public class NEOResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	@JacksonXmlProperty(localName = "s", namespace = VASNamespace.NAMESPACE)
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("s")
	private String status;

	@JacksonXmlProperty(localName = "sm", namespace = VASNamespace.NAMESPACE)
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("sm")
	private String statusMessage;

	@JacksonXmlProperty(localName = "p", namespace = VASNamespace.NAMESPACE)
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("p")
	private List<NEOParamPool> paramPool = new ArrayList<NEOParamPool>();

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the statusMessage
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

	/**
	 * @param statusMessage
	 *            the statusMessage to set
	 */
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	/**
	 * @return the paramPool
	 */
	public List<NEOParamPool> getParamPool() {
		return paramPool;
	}

	/**
	 * @param paramPool
	 *            the paramPool to set
	 */
	public void setParamPool(List<NEOParamPool> paramPool) {
		this.paramPool = paramPool;
	}

	public void addMapParameter(Map<String, String> mapParam) {
		for (Map.Entry<String, String> entity : mapParam.entrySet()) {
			if (entity.getKey() != null && !entity.getKey().equals("") && entity.getValue() != null && !entity.getValue().equals("")) {
				this.paramPool.add(new NEOParamPool(entity.getKey(), entity.getValue()));
			}
		}
	}
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < paramPool.size(); i++) {
			map.put(paramPool.get(i).getKey(), paramPool.get(i).getValue());
		}
		return map;
	}
	@Override
	public String toString() {
		return "NEOResponse [status=" + status + ", statusMessage=" + statusMessage + ", paramPool=" + paramPool + "]";
	}
}
