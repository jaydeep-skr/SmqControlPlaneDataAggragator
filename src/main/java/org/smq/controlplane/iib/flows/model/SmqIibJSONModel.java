package org.smq.controlplane.iib.flows.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SmqIibJSONModel {
	private String flowName;
	private String techFlowType;
	private String flowType;
	private String iibNodeName;
	private String iibServerName;
	private String iibApplicationName;
	private String iibQueueManagerName;
	private String environmentName;
	private String smqOnboardedAppName;

	private SMQIIBQueueManagerDetails sourceQMDetails;
	private SMQIIBQueueManagerDetails destinationQMDetails;

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getTechFlowType() {
		return techFlowType;
	}

	public void setTechFlowType(String techFlowType) {
		this.techFlowType = techFlowType;
	}

	public String getFlowType() {
		return flowType;
	}

	public void setFlowType(String flowType) {
		this.flowType = flowType;
	}

	public String getiibNodeName() {
		return iibNodeName;
	}

	public void setiibNodeName(String iibNodeName) {
		this.iibNodeName = iibNodeName;
	}

	public String getiibServerName() {
		return iibServerName;
	}

	public void setiibServerName(String iibServerName) {
		this.iibServerName = iibServerName;
	}

	public String getiibQueueManagerName() {
		return iibQueueManagerName;
	}

	public void setiibQueueManagerName(String iibQueueManagerName) {
		this.iibQueueManagerName = iibQueueManagerName;
	}

	public String getiibApplicationName() {
		return iibApplicationName;
	}

	public void setiibApplicationName(String iibApplicationName) {
		this.iibApplicationName = iibApplicationName;
	}
	
	public SMQIIBQueueManagerDetails getSourceQMDetails() {
		return sourceQMDetails;
	}

	public void setSourceQMDetails(SMQIIBQueueManagerDetails sourceQMDetails) {
		this.sourceQMDetails = sourceQMDetails;
	}

	public SMQIIBQueueManagerDetails getDestinationQMDetails() {
		return destinationQMDetails;
	}

	public void setDestinationQMDetails(SMQIIBQueueManagerDetails destinationQMDetails) {
		this.destinationQMDetails = destinationQMDetails;
	}
	
	
	public String getIibNodeName() {
		return iibNodeName;
	}

	public void setIibNodeName(String iibNodeName) {
		this.iibNodeName = iibNodeName;
	}

	public String getIibServerName() {
		return iibServerName;
	}

	public void setIibServerName(String iibServerName) {
		this.iibServerName = iibServerName;
	}

	public String getIibQueueManagerName() {
		return iibQueueManagerName;
	}

	public void setIibQueueManagerName(String iibQueueManagerName) {
		this.iibQueueManagerName = iibQueueManagerName;
	}

	public String getenvironmentName() {
		return environmentName;
	}

	public void setenvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}

	public String getSmqOnboardedAppName() {
		return smqOnboardedAppName;
	}

	public void setSmqOnboardedAppName(String smqOnboardedAppName) {
		this.smqOnboardedAppName = smqOnboardedAppName;
	}

	@SuppressWarnings("deprecation")
	public ObjectNode toJSONObject() {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("flowName", this.flowName);
		objectNode.put("techFlowType", this.techFlowType);
		objectNode.put("flowType", this.flowType);
		objectNode.put("iibNodeName", this.iibNodeName);
		objectNode.put("iibServerName", this.iibServerName);
		objectNode.put("iibApplicationName", this.iibApplicationName);
		objectNode.put("iibQueueManagerName", this.iibQueueManagerName);
		objectNode.put("smqOnboardedAppName", this.smqOnboardedAppName);
		objectNode.put("environmentName", this.environmentName);
		objectNode.put("sourceApp", this.sourceQMDetails.toJSONObject());
		objectNode.put("destinationApp", this.destinationQMDetails.toJSONObject());

		return objectNode;
	}

	@SuppressWarnings("deprecation")
	public String toJSONString() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("flowName", this.flowName);
		objectNode.put("techFlowType", this.techFlowType);
		objectNode.put("flowType", this.flowType);
		objectNode.put("iibNodeName", this.iibNodeName);
		objectNode.put("iibServerName", this.iibServerName);
		objectNode.put("iibApplicationName", this.iibApplicationName);
		objectNode.put("iibQueueManagerName", this.iibQueueManagerName);
		//objectNode.put("iibQueueManagerName", this.iibQueueManagerName);
		objectNode.put("smqOnboardedAppName", this.smqOnboardedAppName);
		objectNode.put("sourceApp", this.sourceQMDetails.toJSONObject());
		objectNode.put("destinationApp", this.destinationQMDetails.toJSONObject());

		return objectNode.toString();
	}

}
