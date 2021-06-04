package org.smq.controlplane.iib.flows.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MqInfoQueueManagerDetails {
	String queueManagerName;
	String queuemanagerType;
	String clusterName;
	String connectionName;
	String maxMSGL;
	
	public String getQueueManagerName() {
		return queueManagerName;
	}
	public void setQueueManagerName(String queueManagerName) {
		this.queueManagerName = queueManagerName;
	}
	public String getQueuemanagerType() {
		return queuemanagerType;
	}
	public void setQueuemanagerType(String queuemanagerType) {
		this.queuemanagerType = queuemanagerType;
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public String getConnectionName() {
		return connectionName;
	}
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public String getMaxMSGL() {
		return maxMSGL;
	}
	public void setMaxMSGL(String maxMSGL) {
		this.maxMSGL = maxMSGL;
	}
	
	
	public ObjectNode toJSONObject() {		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("queueManagerName", this.queueManagerName);
		objectNode.put("queuemanagerType", this.queuemanagerType);
		objectNode.put("clusterName", this.clusterName);
		objectNode.put("connectionName", this.connectionName);
		objectNode.put("maxMSGL", this.maxMSGL);
		return objectNode;
	}

	
	public String toJSONString() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("queueManagerName", this.queueManagerName);
		objectNode.put("queuemanagerType", this.queuemanagerType);
		objectNode.put("clusterName", this.clusterName);
		objectNode.put("connectionName", this.connectionName);
		objectNode.put("maxMSGL", this.maxMSGL);
		return objectNode.toString();
	}

}
