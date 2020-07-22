package com.neo.cohgw.bs;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * @author cimit
 * @Date 04-07-2020
 *
 *       NEOParameter.java
 */
@JacksonXmlRootElement(localName = "p", namespace = VASNamespace.NAMESPACE)
public class NEOParamPool implements Serializable {
	private static final long serialVersionUID = 1L;

	@JacksonXmlProperty(localName = "k", namespace = VASNamespace.NAMESPACE)
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("k")
	private String key;

	@JacksonXmlProperty(localName = "v", namespace = VASNamespace.NAMESPACE)
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("v")
	private String value;

	public NEOParamPool() {
	}

	/**
	 * @param key
	 * @param value
	 */
	public NEOParamPool(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "NEOParameter [key: " + key + ", value: " + value + "]";
	}

}

