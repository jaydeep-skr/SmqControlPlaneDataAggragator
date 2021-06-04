package org.smq.controlplane.iib.flows.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SmqIibCsvFlowMapper {
	private String flowName;
	private String flowType;
	private String envName;
	private String appName;
	private String appRole;
	private String queueName;
	private String queueManagerName;
	private String useSSL;

	
	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getQueueManagerName() {
		return queueManagerName;
	}

	public void setQueueManagerName(String queueManagerName) {
		this.queueManagerName = queueManagerName;
	}

	public String getUseSSL() {
		return useSSL;
	}

	public void setUseSSL(String useSSL) {
		this.useSSL = useSSL;
	}

	public String getAppRole() {
		return appRole;
	}

	public void setAppRole(String appRole) {
		this.appRole = appRole;
	}
	

	public String getFlowType() {
		return flowType;
	}

	public void setFlowType(String flowType) {
		this.flowType = flowType;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public ObjectNode toJSONObject() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("flowName", this.flowName);
		objectNode.put("flowType", this.flowType);
		objectNode.put("envName", this.envName);
		objectNode.put("appName", this.appName);
		objectNode.put("appRole", this.appRole);
		objectNode.put("queueName", this.queueName);
		objectNode.put("queueManagerName", this.queueManagerName);
		objectNode.put("useSSL", this.useSSL);
		return objectNode;
	}

	public String toJSONString() {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("flowName", this.flowName);
		objectNode.put("flowType", this.flowType);
		objectNode.put("envName", this.envName);
		objectNode.put("appName", this.appName);
		objectNode.put("appRole", this.appRole);
		objectNode.put("queueName", this.queueName);
		objectNode.put("queueManagerName", this.queueManagerName);
		objectNode.put("useSSL", this.useSSL);

		return objectNode.toString();
	}


}
