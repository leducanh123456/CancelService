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
 *       NEORequest.java
 */
@JacksonXmlRootElement(localName = "req")
public class NEORequest implements Serializable {
	private static final long serialVersionUID = 1L;
	@JacksonXmlProperty(localName = "p")
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("p")
	private List<NEOParamPool> paramPool = new ArrayList<NEOParamPool>();

	public void addParameter(String name, String value) {
		if (name != null && !name.equals("") && value != null && !value.equals("")) {
			this.paramPool.add(new NEOParamPool(name, value));
		}
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

	/**
	 * @return the paramPool
	 */
	public List<NEOParamPool> getParamPool() {
		return paramPool;
	}

	/**
	 * @param paramPool the paramPool to set
	 */
	public void setParamPool(List<NEOParamPool> paramPool) {
		this.paramPool = paramPool;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("Parameters [ ");
		for (int i = 0; i < paramPool.size(); i++) {
			result.append(paramPool.get(i).toString()).append(" ,");
		}
		result.deleteCharAt(result.length() - 1);
		result.append("]");
		return result.toString();
	}
}
