package org.smq.controlplane.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.smq.controlplane.iib.flows.model.SmqIibCsvFlowMapper;
import org.smq.controlplane.iib.flows.model.SMQIIBQueueManagerDetails;
import org.smq.controlplane.iib.flows.model.SmqIibJSONModel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IIBLogFileParser {
	
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObjectNode objectNode = objectMapper.createObjectNode();
    private ArrayNode arrayNode = objectMapper.createArrayNode();
    private String iibIntegrationNodeName = "";
    private String iibQueueManagerName = "";
    private HashMap<String, String> iibFLowMapperJSON = new HashMap<String,String>();
    private Map<String, List<SmqIibCsvFlowMapper>> smqIIBFlowQMMapper = new HashMap<String, List<SmqIibCsvFlowMapper>>();
    private MQInfoParser mqInfo = new MQInfoParser();
    
    public void buildIIBMapperCache(String iibExtractDir) {
    	IIBExtractCsvParser iibExtractParser = new IIBExtractCsvParser();
    	iibExtractParser.processIIBFExtracts(iibExtractDir);
    	this.smqIIBFlowQMMapper = iibExtractParser.getIIBFlowMapper();		
    }
    

    public void buildMQInfoMapperCache(String mqDumpdir, String mqDumpJSONdir) {
		try {
			mqInfo.processMQInfoDumpFiles(mqDumpdir, mqDumpJSONdir);
		}
		catch(Exception e)
		{
			System.out.println("Error occurred in processing MQ Info file");
			e.printStackTrace();
		}
    }
   
    public void processIIBFLogs(String iibLogDir, String iibExtractDir, String mqDumpdir, String mqDumpJSONdir)  {
    	
    	File lookupDir = new File(iibLogDir);
    	String[] list = lookupDir.list(new FilenameFilter() {
    	    @Override
    	    public boolean accept(File dir, String name) {
    	        return name.endsWith(LogParserConstants.SMQ_IIB_LOG_FILE_EXTENSION);
    	    }
    	});

		Set<String> fileList = new HashSet<>(Arrays.asList(list));		
		buildIIBMapperCache(iibExtractDir);
		buildMQInfoMapperCache(mqDumpdir, mqDumpJSONdir);
		
		fileList.forEach(file -> {
			try {
				readFile(iibLogDir, file);
				
			} catch (IOException e) {
				System.out.println("Unknown exception occurred while reading the directory " + iibLogDir + " " + e.getMessage());
				e.printStackTrace();
			}
		});		
		
    }
    
	public void readFile(String fileDirectory, String file) throws IOException {	
		
		String filePath = fileDirectory + File.separator + file;
		System.out.println("Reading IIB log file >> " + filePath);
		
		try (Stream<String> logStream = Files.lines(Paths.get(filePath))) {			
			logStream.forEach(x -> {
				try {
					parseLogLine(x);
				} catch (Exception e) {
					System.out.println("Exception occurred while parsing logger of the IIB file " + e.getMessage());
					e.printStackTrace();
				}
			});
			
		} catch (IOException ex) {
			System.out.println("Exception occurred in readFile " + ex.getMessage());
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			System.out.println("Unknow exception occurred while reading the IIB file " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		
	}

	public void parseLogLine(Object loggerLine) throws Exception {
		
        String logLine = (String) loggerLine;
        String iibIntegrationServer, iibApplicationName, smqEnvironment;
        iibIntegrationServer = iibApplicationName = smqEnvironment = "";
        
		if (!this.arrayNode.isEmpty()) this.arrayNode.removeAll();
		if (!this.objectNode.isEmpty()) this.objectNode.removeAll();
		
        Pattern pattern = Pattern.compile(LogParserConstants.SMQ_IIB_QUEUE_MANAGER_PATTERN);
		Matcher matcher = pattern.matcher(logLine);
		if (matcher.find())	
			parseIIBNodeQueueManager(logLine);
		
		String flowName = "";
		pattern = Pattern.compile(LogParserConstants.SMQ_IIB_LOGGER_START_REGEX_PATTERN, Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(logLine);
				
		try
		{
			if (matcher.find())
			{
				//System.out.println("logger line " +loggerLine);
				if (logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_START_PATTERN, 0) >= 0)
				{
					if (logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_FEED_END_PATTERN, logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_START_PATTERN, 0)) >= 0) {
						flowName = logLine.substring(logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_START_PATTERN, 0), 
								logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_FEED_END_PATTERN, 0) + LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_FEED_END_PATTERN.length());
					} else {
						if (logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_REQUEST_END_PATTERN, logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_START_PATTERN, 0)) >= 0) {
							flowName = logLine.substring(logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_START_PATTERN, 0), 
									logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_REQUEST_END_PATTERN, 0) + LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_REQUEST_END_PATTERN.length());
						} else {
							if (logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_REPLY_END_PATTERN, logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_START_PATTERN, 0)) >= 0) {
								flowName = logLine.substring(logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_START_PATTERN, 0), 
										logLine.indexOf(LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_REPLY_END_PATTERN, 0) + LogParserConstants.SMQ_IIB_FLOW_NAME_REGEX_REPLY_END_PATTERN.length());
							} 
						}
					}
					
					//Integration server & IIB Application name
					if (logLine.indexOf(LogParserConstants.SMQ_IIB_APPLICATION_RUNNING_PATTERN, 0) >= 0) {
						iibIntegrationServer = logLine.substring(logLine.indexOf(LogParserConstants.SMQ_IIB_INTEGRATION_SERVER_PATTERN, 0) + LogParserConstants.SMQ_IIB_INTEGRATION_SERVER_PATTERN.length() + 1, 
								logLine.indexOf(LogParserConstants.SMQ_IIB_APPLICATION_RUNNING_PATTERN,0) - 1);
						iibApplicationName = logLine.substring(logLine.indexOf(LogParserConstants.SMQ_IIB_APPLICATION_RUNNING_PATTERN, 0) + LogParserConstants.SMQ_IIB_APPLICATION_RUNNING_PATTERN.length() + 1, 
								logLine.indexOf(LogParserConstants.SMQ_IIB_LIBRARY_PATTERN,0) - 1);
					}
					else if (logLine.indexOf(LogParserConstants.SMQ_IIB_APPLICATION_STOPPED_PATTERN, 0) >= 0) {
						iibIntegrationServer = logLine.substring(logLine.indexOf(LogParserConstants.SMQ_IIB_INTEGRATION_SERVER_PATTERN, 0) + LogParserConstants.SMQ_IIB_INTEGRATION_SERVER_PATTERN.length() + 1, 
								logLine.indexOf(LogParserConstants.SMQ_IIB_APPLICATION_STOPPED_PATTERN,0) - 1);
						iibApplicationName = logLine.substring(logLine.indexOf(LogParserConstants.SMQ_IIB_APPLICATION_STOPPED_PATTERN, 0) + LogParserConstants.SMQ_IIB_APPLICATION_STOPPED_PATTERN.length() + 1, 
								logLine.indexOf(LogParserConstants.SMQ_IIB_LIBRARY_PATTERN,0) - 1);
					}
				}
			}
			
		} catch (Exception e) {
			System.out.println("Parsing IIB Flow log message failed due to = " + e.getMessage());
			System.out.println(logLine);
			e.printStackTrace();
			throw e;
		}

		if (flowName.length() > 0) {
			try {	
				createJSON(flowName, 
						iibIntegrationServer, 
						iibApplicationName);	
			} catch (Exception e) {
				System.out.println("JSON creation failed due to = " + e.getMessage());
				System.out.println(logLine);
				e.printStackTrace();
				throw e;
			}
		}
		
	}
	
	public void parseIIBNodeQueueManager(String logLine) {
		
        String iibIntegrationNode, iibQueueManager;
        iibIntegrationNode = iibQueueManager = "";

        Pattern pattern = Pattern.compile(LogParserConstants.SMQ_IIB_QUEUE_MANAGER_PATTERN);
		Matcher matcher = pattern.matcher(logLine);

		if (matcher.find())
		{
			//Queue Manager name & IIB Node
			if (logLine.indexOf(LogParserConstants.SMQ_IIB_DEFAULT_QUEUE_MANAGER_PATTERN, 0) >= 0)
			{
				if (logLine.indexOf(LogParserConstants.SMQ_IIB_INTEGRATION_NODE_START_PATTERN, 0) >= 0) {

					iibIntegrationNode = logLine.substring(logLine.indexOf(LogParserConstants.SMQ_IIB_INTEGRATION_NODE_START_PATTERN, 0) + LogParserConstants.SMQ_IIB_INTEGRATION_NODE_START_PATTERN.length() + 1, 
							logLine.indexOf(LogParserConstants.SMQ_IIB_DEFAULT_QUEUE_MANAGER_PATTERN,0) - 1);
					iibQueueManager = logLine.substring(logLine.indexOf(LogParserConstants.SMQ_IIB_DEFAULT_QUEUE_MANAGER_PATTERN, 0) + LogParserConstants.SMQ_IIB_DEFAULT_QUEUE_MANAGER_PATTERN.length() + 1, 
							logLine.indexOf(LogParserConstants.SMQ_IIB_ADMIN_URI_START_PATTERN1,0) - 1);
				}				
			} else if (logLine.indexOf(LogParserConstants.SMQ_IIB_ACTIVE_MULTI_INSTANCE_QUEUE_MANAGER_PATTERN, 0) >= 0) {
				
				//BIP1376I: Integration node 'RI0003' is an active multi-instance or High Availability integration node that is running on queue manager 'RQ0003'. The administration URI is 'http://scrbsmqdk008037.crb.apmoller.net:4414' 
				if (logLine.indexOf(LogParserConstants.SMQ_IIB_INTEGRATION_NODE_START_PATTERN, 0) >= 0) {
					iibIntegrationNode = logLine.substring(logLine.indexOf(LogParserConstants.SMQ_IIB_INTEGRATION_NODE_START_PATTERN, 0) + LogParserConstants.SMQ_IIB_INTEGRATION_NODE_START_PATTERN.length() + 1, 
							logLine.indexOf(LogParserConstants.SMQ_IIB_ACTIVE_MULTI_INSTANCE_QUEUE_MANAGER_PATTERN,0) - 1);
					iibQueueManager = logLine.substring(logLine.indexOf(LogParserConstants.SMQ_IIB_ACTIVE_MULTI_INSTANCE_QUEUE_MANAGER_PATTERN, 0) + LogParserConstants.SMQ_IIB_ACTIVE_MULTI_INSTANCE_QUEUE_MANAGER_PATTERN.length() + 1, 
							logLine.indexOf(LogParserConstants.SMQ_IIB_ADMIN_URI_START_PATTERN2,0) - 1);
				}
			}
		} 
		
		this.iibIntegrationNodeName = iibIntegrationNode;
		this.iibQueueManagerName = iibQueueManager;		
	}
	
	
	public void createJSON(String flowName,
			String iibIntegrationServer,
			String iibApplicationName) throws Exception {
		
		try
		{
			if (flowName.length() > 0)
			{
				if ( this.smqIIBFlowQMMapper
						.entrySet()
						.stream()
						.filter(f -> f.getKey().equalsIgnoreCase(flowName))
						.count() == 0) {
					//System.out.println("not found "+flowName);
					return;
				}

				
				SmqIibJSONModel iibFlow = extractFlowAttributes(flowName, 
						iibIntegrationServer, 
						iibApplicationName);
				
				ObjectNode oNode = this.objectMapper.createObjectNode();
				oNode = iibFlow.toJSONObject() ;
				this.arrayNode.add(oNode);	
	            this.objectNode.put("smqFlows", arrayNode);	 
	            
	            this.iibFLowMapperJSON.put(flowName, this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode));
	            //System.out.println("Json output " + this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode));
			}
			
		} catch (Exception e)
		{
			System.out.println("Error occurred while forming JSON" + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public SmqIibJSONModel extractFlowAttributes(String flowName,
			String iibIntegrationServer,
			String iibApplicationName) {

		SmqIibJSONModel iibJSON = new SmqIibJSONModel();	
		iibJSON.setFlowName(flowName);
		iibJSON.setTechFlowType(LogParserConstants.SMQ_TECHNICAL_FLOWTYPE);
		iibJSON.setiibNodeName(this.iibIntegrationNodeName);
		iibJSON.setiibServerName(iibIntegrationServer);
		iibJSON.setiibApplicationName(iibApplicationName);
		iibJSON.setiibQueueManagerName(this.iibQueueManagerName);

		List<SmqIibCsvFlowMapper> listFlow = this.smqIIBFlowQMMapper
				.entrySet()
				.stream()
				.filter(f -> f.getKey().equalsIgnoreCase(flowName))
				.map(Map.Entry::getValue)
	    		.flatMap(List::stream)
	    		.collect(Collectors.toList());

		
		Optional<SmqIibCsvFlowMapper> iibSourceFlows = listFlow
				.stream()
				.filter(x -> LogParserConstants.SMQ_IIB_SOURCE_QM_TAG__PATTERN.equals(x.getAppRole().trim()))
				.findAny();

		
		SMQIIBQueueManagerDetails iibSrcQMModel = new SMQIIBQueueManagerDetails(); 
		if (iibSourceFlows.isPresent() )
		{
			iibSrcQMModel.setQueueManagerName(iibSourceFlows.get().getQueueManagerName());	
			iibSrcQMModel.setQueueName(iibSourceFlows.get().getQueueName());
			iibSrcQMModel.setAppName(iibSourceFlows.get().getAppName());
			iibSrcQMModel.setAppRole(iibSourceFlows.get().getAppRole());
			
			iibJSON.setFlowType(iibSourceFlows.get().getFlowType());
			iibJSON.setenvironmentName(iibSourceFlows.get().getEnvName());
			iibJSON.setSmqOnboardedAppName(iibSourceFlows.get().getAppName());

		} else {
			iibSrcQMModel.setQueueManagerName("");
			iibSrcQMModel.setQueueName("");
			iibSrcQMModel.setAppName("");
			iibSrcQMModel.setAppRole("");

			iibJSON.setFlowType("");
			iibJSON.setenvironmentName("");
			iibJSON.setSmqOnboardedAppName("");
		}
		
		Optional<SmqIibCsvFlowMapper> iibDestinationFlows = listFlow
				.stream()
				.filter(Objects::nonNull)
				.filter(x -> LogParserConstants.SMQ_IIB_DESTINATION_QM_TAG_PATTERN.equals(x.getAppRole().trim()))	
				.findAny();

		SMQIIBQueueManagerDetails iibDestQMModel = new SMQIIBQueueManagerDetails();

		if (iibDestinationFlows.isPresent()) {
			iibDestQMModel.setQueueManagerName(iibDestinationFlows.get().getQueueManagerName());
			iibDestQMModel.setQueueName(iibDestinationFlows.get().getQueueName());
			iibDestQMModel.setAppName(iibDestinationFlows.get().getAppName());
			iibDestQMModel.setAppRole(iibDestinationFlows.get().getAppRole());			

			iibJSON.setFlowType(iibDestinationFlows.get().getFlowType());
			iibJSON.setenvironmentName(iibDestinationFlows.get().getEnvName());
			iibJSON.setSmqOnboardedAppName(iibDestinationFlows.get().getAppName());
			
		} else {
			iibDestQMModel.setQueueManagerName("");
			iibDestQMModel.setQueueName("");
			iibDestQMModel.setAppName("");
			iibDestQMModel.setAppRole("");		

			iibJSON.setFlowType("");
			iibJSON.setenvironmentName("");
			iibJSON.setSmqOnboardedAppName("");
		}
		iibJSON.setSourceQMDetails(iibSrcQMModel);		
		iibJSON.setDestinationQMDetails(iibDestQMModel);

		return iibJSON;
	}
	

	public void writeToJSONFiles(String dir) {

		try	{
			this.iibFLowMapperJSON.forEach(( k, v) -> {
				String filePath = dir + File.separator + k + LogParserConstants.SMQ_JSON_FILE_EXTENSION;
				//System.out.println("flow name = " + k);
				//System.out.println("json contents = " + v);
				try {				
					
					BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
					writer.write(v);	
					writer.close();
				} catch (IOException e) {
					System.out.println("Error occurred while allocating buffer for writing JSON" + e.getMessage());
					e.printStackTrace();
				}			
			});			
		    
		}
		catch (Exception e)
		{
			System.out.println("Error occrred while writing JSON" + e.getMessage());
			e.printStackTrace();
		}
	}
}

