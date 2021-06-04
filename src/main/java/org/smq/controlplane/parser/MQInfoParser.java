package org.smq.controlplane.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.smq.controlplane.iib.flows.model.MqInfoQueueDetails;
import org.smq.controlplane.iib.flows.model.MqInfoQueueManagerDetails;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MQInfoParser {
	
	public enum MQObjectType {
	    BEGINNING_OF_QUEUEMANAGER_SECTION_INDICATOR,
	    BEGINNING_OF_QUEUE_SECTION_INDICATOR,
	    BEGINNING_OF_CHANNEL_SECTION_INDICATOR,
	    BEGINNING_OF_CHANNEL_STATUS_SECTION_INDICATOR,
	    BEGINNING_OF_CLUSTERQUEUEMANAGER_SECTION_INDICATOR,
	    BEGINNING_OF_CLUSTERQUEUE_SECTION_INDICATOR,
	    BEGINNING_OF_PROCESS_SECTION_INDICATOR,
	    BEGINNING_OF_NAMELIST_SECTION_INDICATOR,
	    BEGINNING_OF_QUEUE_STATUS_SECTION_INDICATOR,
	    BEGINNING_OF_TOPIC_SECTION_INDICATOR,
	    BEGINNING_OF_LISTENER_SECTION_INDICATOR,
	    BEGINNING_OF_END_SECTION_INDICATOR,
		NOT_A_START_INDICATOR,
		NOT_SET;
	}
	
    private HashMap<String, String> mqInfoMapperJSON = new HashMap<String,String>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObjectNode objectNode = objectMapper.createObjectNode();
    //private ArrayNode arrayNodeQM = objectMapper.createArrayNode();
    private ArrayNode arrayNodeQueues = objectMapper.createArrayNode();
    

	private MQObjectType ongoingMQObjType = MQObjectType.NOT_SET;	
    private String queueManagerName, queueName = "";
    private String starterBlock = LogParserConstants.SMQ_NOT_STARTER;
    boolean firstIntermediateLineinBlock = true, hassMQObjectChangedOver = false, firstTimer = true;
    
    private List<MqInfoQueueManagerDetails> qmDetList = new ArrayList<>();
    private List<MqInfoQueueDetails> queueDetList = new ArrayList<>();

    private MqInfoQueueManagerDetails qmDetls = null;
    private MqInfoQueueDetails queueDetls = null;
    
    //private HashMap<String, String> mqInfoJSON = new HashMap<String,String>();
    //private Map<String, List<MqInfoQueueManagerDetails>> mqInfoQMDetailMapper = new HashMap<String, List<MqInfoQueueManagerDetails>>();
    private Map<String, List<MqInfoQueueDetails>> mqInfoQueueDetailMapper = new HashMap<String, List<MqInfoQueueDetails>>();
	
    public void processMQInfoDumpFiles(String mqDumpDir, String mqInfoJSONDir)  {
    	File lookupDir = new File(mqDumpDir);
    	String[] list = lookupDir.list(new FilenameFilter() {
    	    @Override
    	    public boolean accept(File dir, String name) {
    	        return name.endsWith(LogParserConstants.SMQ_MQINFO_FILE_EXTENSTION);
    	    }
    	});
		Set<String> fileList = new HashSet<>(Arrays.asList(list));		
		fileList.forEach(file -> {
			try {
				readFile(mqDumpDir, file, mqInfoJSONDir);	
			} catch (Exception e) {
				System.out.println("Unknown exception occurred while reading the directory " + mqDumpDir + " " + e.getMessage());
				e.printStackTrace();
			}
		});		
    }

    public List<MqInfoQueueManagerDetails> getQMList() {
    	return qmDetList;
    }

    public List<MqInfoQueueDetails> getQueueList() {
    	return queueDetList;
    }
    
	public void readFile(String dumpFIleDirectory, String file, String mqInfoJSONDir) throws Exception {	

		String filePath = dumpFIleDirectory + File.separator + file;
		System.out.println("Reading MQInfo file >> " + filePath);

		if (file.indexOf(LogParserConstants.SMQ_MQINFO_FILE_EXTENSTION, 0) >= 0) 
			queueManagerName = file.substring(0, file.indexOf(LogParserConstants.SMQ_MQINFO_FILE_EXTENSTION, 0) ); 

		queueDetList.clear();
		
		try (Stream<String> logStream = Files.lines(Paths.get(filePath)).skip(4)) {			
			logStream.forEach(x -> {
				try {
					MQObjectType mqObjType = checkMQObjectType(x);					
					if (ongoingMQObjType == MQObjectType.NOT_SET) {						
						ongoingMQObjType = mqObjType;
						return;
					} else if ((ongoingMQObjType != MQObjectType.NOT_SET) && (starterBlock != LogParserConstants.SMQ_NOT_STARTER)) {
						if ((!hassMQObjectChangedOver) && (!firstTimer)) {
							if (ongoingMQObjType == MQObjectType.BEGINNING_OF_CLUSTERQUEUEMANAGER_SECTION_INDICATOR) {
								qmDetList.add(qmDetls);
							}
						}
						firstIntermediateLineinBlock = true;
						hassMQObjectChangedOver = false;
						return;						
					} else if ((mqObjType == MQObjectType.NOT_A_START_INDICATOR) && (starterBlock == LogParserConstants.SMQ_NOT_STARTER)) {
						populateMQObject(ongoingMQObjType, x, queueManagerName);
					} else if ((mqObjType != MQObjectType.NOT_A_START_INDICATOR) && (mqObjType != ongoingMQObjType)) {
						if (ongoingMQObjType == MQObjectType.BEGINNING_OF_CLUSTERQUEUEMANAGER_SECTION_INDICATOR) {
							qmDetList.add(qmDetls);							
						} else if (ongoingMQObjType == MQObjectType.BEGINNING_OF_QUEUE_SECTION_INDICATOR) {
							/*
							if (queueDetls.getQueueName() != null) {
								if (!queueDetList.stream().filter(q -> queueDetls.getQueueName().equals(q.getQueueName())).findAny().isPresent())
									queueDetList.add(queueDetls);
							}
							*/
						}
						hassMQObjectChangedOver = true;
						firstIntermediateLineinBlock = true;
						firstTimer = false;
						populateMQObject(mqObjType, x, queueManagerName);
						ongoingMQObjType = mqObjType;
					}
				} catch (Exception e) {
					System.out.println("Exception occurred while parsing MQInfo file " + e.getMessage());
					e.printStackTrace();
				}
			});

			//if (!this.arrayNodeQM.isEmpty()) this.arrayNodeQM.removeAll();
			if (!this.arrayNodeQueues.isEmpty()) this.arrayNodeQueues.removeAll();
			if (!this.objectNode.isEmpty()) this.objectNode.removeAll();
			//queueDetList.stream().forEach(x -> System.out.println(x.getQueueName()));
			updateQueueManagerInfoInQueue();
			createJSON();
			writeToJSONFiles(mqInfoJSONDir);
			
		} catch (IOException ex) {
			System.out.println("Exception occurred in readFile " + ex.getMessage());
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			System.out.println("Unknow exception occurred while reading the MQInfo file " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	public MQObjectType checkMQObjectType(Object loggerLine) {
		
        String logLine = (String) loggerLine;

        // check if "DISPLAY QMGR ALL";
        Pattern pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_QUEUE_MANAGER_DETAILS_START_TAG);
		Matcher matcher = pattern.matcher(logLine);
		if (matcher.find())	
			return MQObjectType.BEGINNING_OF_QUEUEMANAGER_SECTION_INDICATOR;

		
		// check if "DISPLAY Q(*) ALL CLUSINFO";
		pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_QUEUE_DETAILS_START_TAG);
		matcher = pattern.matcher(logLine);
		if (matcher.find())	
			return MQObjectType.BEGINNING_OF_QUEUE_SECTION_INDICATOR;
		
		// check if "DISPLAY CHL(*) ALL";
		pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_CHANNEL_DETAILS_START_TAG);
		matcher = pattern.matcher(logLine);
		if (matcher.find())	{
			return MQObjectType.BEGINNING_OF_CHANNEL_SECTION_INDICATOR;
		}

		// check if "DISPLAY CHS(*) ALL";
		pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_CHANNEL_STATUS_START_TAG);
		matcher = pattern.matcher(logLine);
		if (matcher.find())	{
			return MQObjectType.BEGINNING_OF_CHANNEL_STATUS_SECTION_INDICATOR;
		}
			
		// check if "DISPLAY CLUSQMGR(*) ALL";
		pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_CLUSTER_QM_DETAILS_START_TAG);
		matcher = pattern.matcher(logLine);
		if (matcher.find())	{
			return MQObjectType.BEGINNING_OF_CLUSTERQUEUEMANAGER_SECTION_INDICATOR;
		}
		
		// check if "DISPLAY QC(*) ALL";
		pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_CLUSTER_QUEUE_DETAILS_START_TAG);
		matcher = pattern.matcher(logLine);
		if (matcher.find())	{
			return MQObjectType.BEGINNING_OF_CLUSTERQUEUE_SECTION_INDICATOR;
		}
		
		// check if "DISPLAY PRO(*) ALL";
		pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_PROCESS_STATUS_START_TAG);
		matcher = pattern.matcher(logLine);
		if (matcher.find())	{
			return MQObjectType.BEGINNING_OF_PROCESS_SECTION_INDICATOR;
		}
		
		// check if "DISPLAY NL(*) ALL";
		pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_NAMELIST_DETAILS_START_TAG);
		matcher = pattern.matcher(logLine);
		if (matcher.find())	{
			return MQObjectType.BEGINNING_OF_NAMELIST_SECTION_INDICATOR;
		}
		// check if "DISPLAY QSTATUS(*) ALL";
		pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_QUEUE_STATUS_START_TAG);
		matcher = pattern.matcher(logLine);
		if (matcher.find())	{
			return MQObjectType.BEGINNING_OF_QUEUE_STATUS_SECTION_INDICATOR;
		}
		
		// check if "DISPLAY TOPIC(*) ALL";
		pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_TOPIC_DETAILS_START_TAG);
		matcher = pattern.matcher(logLine);
		if (matcher.find())	{
			return MQObjectType.BEGINNING_OF_TOPIC_SECTION_INDICATOR;
		}
		// check if "DISPLAY LISTENER(*) ALL";
		pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_LISTERNER_DETAILS_START_TAG);
		matcher = pattern.matcher(logLine);
		if (matcher.find())	{
			return MQObjectType.BEGINNING_OF_LISTENER_SECTION_INDICATOR;
		}
		
		// check if "12 : END";
		pattern = Pattern.compile(LogParserConstants.SMQ_MQ_INFO_END_TAG);
		matcher = pattern.matcher(logLine);
		if (matcher.find())	{
			return MQObjectType.BEGINNING_OF_END_SECTION_INDICATOR;
		}
		
		//checking start of a block
		if (ongoingMQObjType == MQObjectType.BEGINNING_OF_QUEUEMANAGER_SECTION_INDICATOR) {
			pattern = Pattern.compile(LogParserConstants.SMQ_QUEUE_MANAGER_DETAILS_START_BLOCK);
			matcher = pattern.matcher(logLine);			
			if (matcher.find())	
				starterBlock = LogParserConstants.SMQ_QUEUE_MANAGER_DETAILS_START_BLOCK;
			else
				starterBlock = LogParserConstants.SMQ_NOT_STARTER;			
		} else if (ongoingMQObjType == MQObjectType.BEGINNING_OF_QUEUE_SECTION_INDICATOR) {
			pattern = Pattern.compile(LogParserConstants.SMQ_QUEUE_DETAILS_START_BLOCK);
			matcher = pattern.matcher(logLine);			
			if (matcher.find())	
				starterBlock = LogParserConstants.SMQ_QUEUE_DETAILS_START_BLOCK;
			else
				starterBlock = LogParserConstants.SMQ_NOT_STARTER;			
		} else if (ongoingMQObjType == MQObjectType.BEGINNING_OF_CHANNEL_SECTION_INDICATOR) {
			pattern = Pattern.compile(LogParserConstants.SMQ_CHANNEL_DETAILS_START_BLOCK);
			matcher = pattern.matcher(logLine);			
			if (matcher.find())	
				starterBlock = LogParserConstants.SMQ_CHANNEL_DETAILS_START_BLOCK;
			else
				starterBlock = LogParserConstants.SMQ_NOT_STARTER;			
		} else if (ongoingMQObjType == MQObjectType.BEGINNING_OF_CHANNEL_STATUS_SECTION_INDICATOR) {
			pattern = Pattern.compile(LogParserConstants.SMQ_CHANNEL_STATUS_START_BLOCK);
			matcher = pattern.matcher(logLine);			
			if (matcher.find())	
				starterBlock = LogParserConstants.SMQ_CHANNEL_STATUS_START_BLOCK;
			else
				starterBlock = LogParserConstants.SMQ_NOT_STARTER;			
		} else if (ongoingMQObjType == MQObjectType.BEGINNING_OF_CLUSTERQUEUEMANAGER_SECTION_INDICATOR) {
			pattern = Pattern.compile(LogParserConstants.SMQ_CLUSQUEUMANAGER_START_BLOCK);
			matcher = pattern.matcher(logLine);			
			if (matcher.find())	
				starterBlock = LogParserConstants.SMQ_CLUSQUEUMANAGER_START_BLOCK;
			else
				starterBlock = LogParserConstants.SMQ_NOT_STARTER;			
		} else if (ongoingMQObjType == MQObjectType.BEGINNING_OF_CLUSTERQUEUE_SECTION_INDICATOR) {
			pattern = Pattern.compile(LogParserConstants.SMQ_CLUSQUEUE_START_BLOCK);
			matcher = pattern.matcher(logLine);			
			if (matcher.find())	
				starterBlock = LogParserConstants.SMQ_CLUSQUEUE_START_BLOCK;
			else
				starterBlock = LogParserConstants.SMQ_NOT_STARTER;			
		} else if (ongoingMQObjType == MQObjectType.BEGINNING_OF_PROCESS_SECTION_INDICATOR) {
			pattern = Pattern.compile(LogParserConstants.SMQ_PROCESS_START_BLOCK);
			matcher = pattern.matcher(logLine);			
			if (matcher.find())	
				starterBlock = LogParserConstants.SMQ_PROCESS_START_BLOCK;
			else
				starterBlock = LogParserConstants.SMQ_NOT_STARTER;			
		} else if (ongoingMQObjType == MQObjectType.BEGINNING_OF_NAMELIST_SECTION_INDICATOR) {
			pattern = Pattern.compile(LogParserConstants.SMQ_NAMELIST_START_BLOCK);
			matcher = pattern.matcher(logLine);			
			if (matcher.find())	
				starterBlock = LogParserConstants.SMQ_NAMELIST_START_BLOCK;
			else
				starterBlock = LogParserConstants.SMQ_NOT_STARTER;			
		} else if (ongoingMQObjType == MQObjectType.BEGINNING_OF_QUEUE_STATUS_SECTION_INDICATOR) {
			pattern = Pattern.compile(LogParserConstants.SMQ_QUEUESTATUS_START_BLOCK);
			matcher = pattern.matcher(logLine);			
			if (matcher.find())	
				starterBlock = LogParserConstants.SMQ_QUEUESTATUS_START_BLOCK;
			else
				starterBlock = LogParserConstants.SMQ_NOT_STARTER;			
		} else if (ongoingMQObjType == MQObjectType.BEGINNING_OF_TOPIC_SECTION_INDICATOR) {
			pattern = Pattern.compile(LogParserConstants.SMQ_TOPIC_START_BLOCK);
			matcher = pattern.matcher(logLine);			
			if (matcher.find())	
				starterBlock = LogParserConstants.SMQ_TOPIC_START_BLOCK;
			else
				starterBlock = LogParserConstants.SMQ_NOT_STARTER;			
		} else if (ongoingMQObjType == MQObjectType.BEGINNING_OF_LISTENER_SECTION_INDICATOR) {
			pattern = Pattern.compile(LogParserConstants.SMQ_LISTENER_START_BLOCK);
			matcher = pattern.matcher(logLine);			
			if (matcher.find())	
				starterBlock = LogParserConstants.SMQ_LISTENER_START_BLOCK;
			else
				starterBlock = LogParserConstants.SMQ_NOT_STARTER;			
		}
		return MQObjectType.NOT_A_START_INDICATOR;
	
	}

	public void populateMQObject(MQObjectType mqObjectType, String logLine, String qmName) {
		
		switch (mqObjectType) {
		case BEGINNING_OF_QUEUEMANAGER_SECTION_INDICATOR:
			/*
			if (firstIntermediateLineinBlock) {
				qmDetls = new MqInfoQueueManagerDetails();
				firstIntermediateLineinBlock = false;
			}			
			parseAndPopulateQMDetails(logLine, qmDetls);
			*/		
			break;
		case BEGINNING_OF_QUEUE_SECTION_INDICATOR:
			if (firstIntermediateLineinBlock) {
				queueName = parseAndGetQueueName(logLine);
				firstIntermediateLineinBlock = false;
			}
			if (queueName.trim().length() > 0)
				parseAndPopulateQueueDetails(logLine, queueName, qmName);
			break;
		case BEGINNING_OF_CHANNEL_SECTION_INDICATOR:
			parseAndPopulateChannelDetails();
			break;
		case BEGINNING_OF_CHANNEL_STATUS_SECTION_INDICATOR:
			parseAndPopulateChannelStatusDetails();
			break;
		case BEGINNING_OF_CLUSTERQUEUEMANAGER_SECTION_INDICATOR:
			if (firstIntermediateLineinBlock) {
				qmDetls = new MqInfoQueueManagerDetails();
				firstIntermediateLineinBlock = false;
			}			
			parseAndPopulateQMDetails(logLine, qmDetls);
			break;
		case BEGINNING_OF_CLUSTERQUEUE_SECTION_INDICATOR:
			parseAndPopulateClusterQueueDetails();
			break;
		case BEGINNING_OF_PROCESS_SECTION_INDICATOR:
			parseAndPopulateProcessDetails();
			break;
		case BEGINNING_OF_NAMELIST_SECTION_INDICATOR:
			parseAndPopulateNamelistDetails();
			break;
		case BEGINNING_OF_QUEUE_STATUS_SECTION_INDICATOR:
			parseAndPopulateQueueStatusDetails();
			break;
		case BEGINNING_OF_TOPIC_SECTION_INDICATOR:
			parseAndPopulateTopicDetails();
			break;
		case BEGINNING_OF_LISTENER_SECTION_INDICATOR:
			parseAndPopulateListenerDetails();
			break;
		case BEGINNING_OF_END_SECTION_INDICATOR:
			break;
		case NOT_A_START_INDICATOR:
			break;
		default:
			break;
		}
		
	}
	
	public void parseAndPopulateQMDetails(String logLine, MqInfoQueueManagerDetails qmDetails) {	

		int startPosofVal, endPosVal = 0;
		
		if (logLine.indexOf(LogParserConstants.MQINFO_CLUSQMGR, 0) >= 0) 	{		
			startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_CLUSQMGR, 0) + LogParserConstants.MQINFO_CLUSQMGR.length() + LogParserConstants.OPEN_BRACKET.length();
		    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
			String qmName = logLine.substring(startPosofVal, endPosVal);
			qmDetails.setQueueManagerName(qmName);
			qmDetails.setQueuemanagerType(LogParserConstants.MQINFO_QCLUSTER_DESC);
		}
		
		if (logLine.indexOf(LogParserConstants.MQINFO_CLUSTER, 0) >= 0) 	{		
			startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_CLUSTER, 0) + LogParserConstants.MQINFO_CLUSTER.length() + LogParserConstants.OPEN_BRACKET.length();
		    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
		    if (logLine.charAt(logLine.indexOf(LogParserConstants.MQINFO_CLUSTER, 0) -1) == ' ') {		    
				String cluster = logLine.substring(startPosofVal, endPosVal);
				qmDetails.setClusterName(cluster);
		    }
		}
		if (logLine.indexOf(LogParserConstants.MQINFO_CONNAME, 0) >= 0) 	{		
			startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_CONNAME, 0) + LogParserConstants.MQINFO_CONNAME.length() + LogParserConstants.OPEN_BRACKET.length();
		    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
			String connName = logLine.substring(startPosofVal, endPosVal+1);
			qmDetails.setConnectionName(connName);
		}

		
		if (logLine.indexOf(LogParserConstants.MQINFO_MAXMSGL, 0) >= 0) 	{		
			startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_MAXMSGL, 0) + LogParserConstants.MQINFO_MAXMSGL.length() + LogParserConstants.OPEN_BRACKET.length();
		    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
			String maxMSGL = logLine.substring(startPosofVal, endPosVal);
			qmDetails.setMaxMSGL(maxMSGL);
		}

		
	}
	
	public String parseAndGetQueueName(String logLine) {
			//Hacking: Had to do bit of hacking at some places as some of the keywords are not unique 
			//and they are often found to be present as part of another keyword
			// Examples: QUEUE, TYPE etc		
			int startPosofVal, endPosVal = 0;
			String queueName = "";
			
			if (logLine.indexOf(LogParserConstants.MQINFO_QUEUENAME, 0) >= 0) {
				//hack: check whether Keyword QUEUE is part of TARGTYPE(QUEUE). if yes then ignore
		        Pattern pattern = Pattern.compile(LogParserConstants.MQINFO_IGNORABLE_QUEUE);
				Matcher matcher = pattern.matcher(logLine);
				if (!matcher.find()) {
					pattern = Pattern.compile(LogParserConstants.MQINFO_IGNORABLE_QUEUE2);
					matcher = pattern.matcher(logLine);
					if (!matcher.find()) {
						startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_QUEUENAME, 0) + LogParserConstants.MQINFO_QUEUENAME.length() + LogParserConstants.OPEN_BRACKET.length();
					    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
						queueName = logLine.substring(startPosofVal, endPosVal);
					}
				}
			}
			return queueName;
	}
	public void parseAndPopulateQueueDetails(String logLine, String queueName, String qmName) {
		
		//Hacking: Had to do bit of hacking at some places as some of the keywords are not unique 
		//and they are often found to be present as part of another keyword
		// Examples: QUEUE, TYPE etc		
		int startPosofVal, endPosVal = 0;
		
		if (logLine.indexOf(LogParserConstants.MQINFO_QUEUENAME, 0) >= 0) 	{
			if (queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).count() == 0) {
				queueDetls = new MqInfoQueueDetails();
				queueDetls.setQueueName(queueName);
			    List<MqInfoQueueManagerDetails> clusterQueueManagerList = new ArrayList<>();
			    queueDetls.setQueueManagerNameList(clusterQueueManagerList);
				queueDetList.add(queueDetls);
			}
		}

		if (logLine.indexOf(LogParserConstants.MQINFO_QUEUETYPE, 0) >= 0) 	{		
			startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_QUEUETYPE, 0) + LogParserConstants.MQINFO_QUEUETYPE.length() + 1;
		    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
			//check whether Keyword TYPE is part of TARGTYPE(QUEUE). if yes then ignore
		    if (logLine.charAt(logLine.indexOf(LogParserConstants.MQINFO_QUEUETYPE, 0) -1) == ' ') {
				String queueType = logLine.substring(startPosofVal, endPosVal);
				if (queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).count() > 0) {
					Optional<MqInfoQueueDetails> queueDet = queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).findAny();
					if ((queueDet.isPresent()) && (!queueType.equals(LogParserConstants.MQINFO_QCLUSTER))) {
						if (queueType.equals(LogParserConstants.MQINFO_QALIAS))
							queueDet.get().setQueueType(LogParserConstants.MQINFO_QALIAS_DESC);
						else if (queueType.equals(LogParserConstants.MQINFO_QLOCALS))
							queueDet.get().setQueueType(LogParserConstants.MQINFO_QLOCALS_DESC);
						List<MqInfoQueueManagerDetails> qManagerList = queueDet.get().getQueueManagerNameList();
						if (qManagerList.size() == 0) {
							MqInfoQueueManagerDetails queueManager = new MqInfoQueueManagerDetails();
							queueManager.setQueueManagerName(qmName);
							qManagerList.add(queueManager);
						} else {
							Optional<MqInfoQueueManagerDetails> qm = qManagerList.stream()
							.filter(QM -> qmName.equals(QM.getQueueManagerName()))
							.findAny();
							if (!qm.isPresent()) {
								MqInfoQueueManagerDetails queueManager = new MqInfoQueueManagerDetails();
								queueManager.setQueueManagerName(qmName);
								qManagerList.add(queueManager);
							}									
						}					
					}
				}
		    } 
		}

		if (logLine.indexOf(LogParserConstants.MQINFO_QUEUEDESCR, 0) >= 0) 	{		
			startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_QUEUEDESCR, 0) + LogParserConstants.MQINFO_QUEUEDESCR.length() + 1;
		    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
			String queueDescr = logLine.substring(startPosofVal, endPosVal);
			if (queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).count() > 0) {
				Optional<MqInfoQueueDetails> queueDet = queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).findAny();
				if (queueDet.isPresent())
					queueDet.get().setDescription(queueDescr);
			}
		}
		

		if (logLine.indexOf(LogParserConstants.MQINFO_CLUSQT, 0) >= 0) 	{		
			startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_CLUSQT, 0) + LogParserConstants.MQINFO_CLUSQT.length() + 1;
		    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
			String clusterQueueType = logLine.substring(startPosofVal, endPosVal);
			if (queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).count() > 0) {
				Optional<MqInfoQueueDetails> queueDet = queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).findAny();
				if (queueDet.isPresent())
					if (clusterQueueType.equals(LogParserConstants.MQINFO_QALIAS))
						queueDet.get().setQueueType(LogParserConstants.MQINFO_QALIAS_DESC);
					else if (clusterQueueType.equals(LogParserConstants.MQINFO_QLOCALS))
						queueDet.get().setQueueType(LogParserConstants.MQINFO_QLOCALS_DESC);
					else
						queueDet.get().setQueueType(clusterQueueType);
			}
		} 
		
		if (logLine.indexOf(LogParserConstants.MQINFO_QUEUETARGET, 0) >= 0) 	{		
			startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_QUEUETARGET, 0) + LogParserConstants.MQINFO_QUEUETARGET.length() + 1;
		    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
			String queueTarget = logLine.substring(startPosofVal, endPosVal);
			if (queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).count() > 0) {
				Optional<MqInfoQueueDetails> queueDet = queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).findAny();
				if (queueDet.isPresent())
					queueDet.get().setTargetQueueName(queueTarget);
			}
		} 
		

		if (logLine.indexOf(LogParserConstants.MQINFO_CLUSTER, 0) >= 0) {	
	        Pattern pattern = Pattern.compile(LogParserConstants.MQINFO_QCLUSTER);
			Matcher matcher = pattern.matcher(logLine);
			if (!matcher.find()) {
				startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_CLUSTER, 0) + LogParserConstants.MQINFO_CLUSTER.length() + 1;
			    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
				String clusterName = logLine.substring(startPosofVal, endPosVal);
				if (queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).count() > 0) {					
					Optional<MqInfoQueueDetails> queueDet = queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).findAny();
					if (queueDet.isPresent()) {
						if (clusterName.trim().length() == 0)
							queueDet.get().setIsClusterQueue(LogParserConstants.MQINFO_IS_NOT_CLUSTER_QUEUE);
						else
							queueDet.get().setIsClusterQueue(LogParserConstants.MQINFO_IS_CLUSTER_QUEUE);						
						queueDet.get().setClusterName(clusterName);
					}
				}
			}
		} 

		if (logLine.indexOf(LogParserConstants.MQINFO_CLUSQMGR, 0) >= 0) 	{		
			startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_CLUSQMGR, 0) + LogParserConstants.MQINFO_CLUSQMGR.length() + 1;
		    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
			String queueManagerName = logLine.substring(startPosofVal, endPosVal);
			if (queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).count() > 0) {
				Optional<MqInfoQueueDetails> queueDet = queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).findAny();
				if (queueDet.isPresent()) {					
					if (queueManagerName.trim().length() == 0)
						queueDet.get().setIsClusterQueue(LogParserConstants.MQINFO_IS_NOT_CLUSTER_QUEUE);
					else
						queueDet.get().setIsClusterQueue(LogParserConstants.MQINFO_IS_CLUSTER_QUEUE);
					List<MqInfoQueueManagerDetails> qManagerList = queueDet.get().getQueueManagerNameList();
					if (qManagerList.size() == 0) {
						MqInfoQueueManagerDetails queueManager = new MqInfoQueueManagerDetails();
						queueManager.setQueueManagerName(queueManagerName);
						qManagerList.add(queueManager);
					} else {
						Optional<MqInfoQueueManagerDetails> qm = qManagerList.stream()
						.filter(QM -> queueManagerName.equals(QM.getQueueManagerName()))
						.findAny();
						if (!qm.isPresent()) {
							MqInfoQueueManagerDetails queueManager = new MqInfoQueueManagerDetails();
							queueManager.setQueueManagerName(queueManagerName);
							qManagerList.add(queueManager);
						}
					}					
				}
			}
		}

		if (logLine.indexOf(LogParserConstants.MQINFO_MAXMSGL, 0) >= 0) 	{		
			startPosofVal = logLine.indexOf(LogParserConstants.MQINFO_MAXMSGL, 0) + LogParserConstants.MQINFO_MAXMSGL.length() + 1;
		    endPosVal= logLine.indexOf(LogParserConstants.CLOSING_BRACKET, startPosofVal);
			String maxMSGL = logLine.substring(startPosofVal, endPosVal);
			if (queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).count() > 0) {
				Optional<MqInfoQueueDetails> queueDet = queueDetList.stream().filter(q -> queueName.equals(q.getQueueName())).findAny();
				if (queueDet.isPresent())
					queueDet.get().setMaxMSGL(maxMSGL);
				else
					queueDet.get().setMaxMSGL("");
					
			} 
		} 

	}
		
	public void parseAndPopulateChannelDetails() {
		
	}
	public void parseAndPopulateChannelStatusDetails() {
		
	}
	public void parseAndPopulateCLusterQMDetails() {
		
	}
	public void parseAndPopulateClusterQueueDetails() {
		
	}
	public void parseAndPopulateProcessDetails() {
		
	}
	public void parseAndPopulateNamelistDetails() {
		
	}
	public void parseAndPopulateQueueStatusDetails() {
		
	}
	public void parseAndPopulateTopicDetails() {
		
	}
	public void parseAndPopulateListenerDetails() {
		
	}

	public void updateQueueManagerInfoInQueue() {
		
		queueDetList.stream().forEach( q -> {
			if (q.getMaxMSGL() == null)
				q.setMaxMSGL("");

			if (q.getTargetQueueName() == null)
				q.setTargetQueueName("");
			
			List<MqInfoQueueManagerDetails> qManagerList = q.getQueueManagerNameList();
			qManagerList.stream().forEach(qm -> {
				Optional<MqInfoQueueManagerDetails> qMGr = qmDetList.stream().filter( QM -> qm.getQueueManagerName().equals(QM.getQueueManagerName())).findAny();
				if (qMGr.isPresent()) {
					//System.out.println("queue manager "+qMGr.get().getQueueManagerName());
					qm.setClusterName(qMGr.get().getClusterName());
					qm.setConnectionName(qMGr.get().getConnectionName());
					qm.setMaxMSGL(qMGr.get().getMaxMSGL());
					qm.setQueuemanagerType(qMGr.get().getQueuemanagerType());
				}
			});
		});
		createMQInfoMapper();		
	}
	public void createMQInfoMapper() {
		
		//create map of Lists with Queue Name as the key
		mqInfoQueueDetailMapper = queueDetList.stream().collect(Collectors.groupingBy(MqInfoQueueDetails::getQueueName));		
	}	

	public void createJSON() throws Exception {
		try
		{
			for(Map.Entry<String, List<MqInfoQueueDetails>> entry : mqInfoQueueDetailMapper.entrySet()) {

				if (!this.arrayNodeQueues.isEmpty()) this.arrayNodeQueues.removeAll();
				if (!this.objectNode.isEmpty()) this.objectNode.removeAll();

			       String key = entry.getKey();
				   List<MqInfoQueueDetails> queueList =  entry.getValue();				    
				    //do something with the key and value
				    queueList.forEach(val -> {
				    	//System.out.println("key "+key);
				    	//System.out.println("node "+val.toJSONString());
			    		ObjectNode oNode = this.objectMapper.createObjectNode();
						oNode = val.toJSONObject() ;
						this.arrayNodeQueues.add(oNode);	
			            this.objectNode.put("queues", arrayNodeQueues);	 	            
			            try {
			            	//System.out.println(key);
							this.mqInfoMapperJSON.put(key, this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode));
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
				    });
				}
	        //System.out.println("Json output " + this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode));
	        //System.out.println("Json output " + this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNodeQueues));
		} catch (Exception e)
		{
			System.out.println("Error occurred while forming JSON" + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	

	public void writeToJSONFiles(String mqInfoJSONDir) {
		System.out.println("writing output");
		//String filePath = "";
		try	{
			this.mqInfoMapperJSON.forEach(( k, v) -> {
				//System.out.println("Key name = " + k);
				//System.out.println("json contents = " + v);
				String filePath = mqInfoJSONDir + File.separator + k + LogParserConstants.SMQ_JSON_FILE_EXTENSION;
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
			System.out.println("Error occurred while writing JSON" + e.getMessage());
			e.printStackTrace();
		}
	}
}
