package org.smq.controlplane.iib.flows.model;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MqInfoQueueDetails {
	String queueName;
	String description;
	String queueType;
	String targetQueueName;
	String isClusterQueue;
	String clusterName;
	String maxMSGL;
	List<MqInfoQueueManagerDetails> queueManagerNameList;
	
	
	public List<MqInfoQueueManagerDetails> getQueueManagerNameList() {
		return queueManagerNameList;
	}
	public void setQueueManagerNameList(List<MqInfoQueueManagerDetails> queueManagerNameList) {
		this.queueManagerNameList = queueManagerNameList;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getQueueType() {
		return queueType;
	}
	public void setQueueType(String queueType) {
		this.queueType = queueType;
	}
	public String getTargetQueueName() {
		return targetQueueName;
	}
	public void setTargetQueueName(String targetQueueName) {
		this.targetQueueName = targetQueueName;
	}
	
	public String getIsClusterQueue() {
		return isClusterQueue;
	}
	public void setIsClusterQueue(String isClusterQueue) {
		this.isClusterQueue = isClusterQueue;
	}
	
	public String getMaxMSGL() {
		return maxMSGL;
	}

	public void setMaxMSGL(String maxMSGL) {
		this.maxMSGL = maxMSGL;
	}

	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public ObjectNode toJSONObject() {		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("queueName", this.queueName);
		objectNode.put("description", this.description);
		objectNode.put("queueType", this.queueType);
		objectNode.put("targetQueueName", this.targetQueueName);
		objectNode.put("maxMSGL", this.maxMSGL);
		objectNode.put("isClusterQueue", this.isClusterQueue);
		objectNode.put("clusterName", this.clusterName);
		ArrayNode array = mapper.valueToTree(queueManagerNameList);
		objectNode.putArray("queueManagers").addAll(array);
		return objectNode;
	}

	
	public String toJSONString() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("queueName", this.queueName);
		objectNode.put("description", this.description);
		objectNode.put("queueType", this.queueType);
		objectNode.put("targetQueueName", this.targetQueueName);
		objectNode.put("maxMSGL", this.maxMSGL);
		objectNode.put("isClusterQueue", this.isClusterQueue);
		objectNode.put("clusterName", this.clusterName);
		ArrayNode array = mapper.valueToTree(queueManagerNameList);
		objectNode.putArray("queueManagers").addAll(array);
		return objectNode.toString();
	}
}
