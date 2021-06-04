package org.smq.controlplane.parser;

public interface LogParserConstants {
	public static final String SMQ_CSV_FILE_EXTENSION = ".csv" ;
	public static final String SMQ_IIB_LOG_FILE_EXTENSION = ".txt" ;
	public static final String SMQ_JSON_FILE_EXTENSION = ".json" ;
	public static final String SMQ_TECHNICAL_FLOWTYPE = "SMQIIB" ;
	public static final String SMQ_IIB_APPLICATION_PREFIX = "SMQIIB_APP" ;
	public static final String SMQ_IIB_LOGGER_START_REGEX_PATTERN = "Message flow" ;
	public static final String SMQ_IIB_FLOW_NAME_REGEX_START_PATTERN = "net" ;
	public static final String SMQ_IIB_FLOW_NAME_REGEX_FEED_END_PATTERN = "Feed" ;
	public static final String SMQ_IIB_FLOW_NAME_REGEX_REQUEST_END_PATTERN = "Request" ;
	public static final String SMQ_IIB_FLOW_NAME_REGEX_REPLY_END_PATTERN = "Reply" ;
	public static final String SMQ_IIB_FLOW_NAME_REGEX_FEED_PREFIX_PATTERN = "net.apmoller.smq.feed" ;
	public static final String SMQ_IIB_FLOW_NAME_REGEX_REQUEST_REPLY_PREFIX_PATTERN = "net.apmoller.smq.rr" ;
	public static final String SMQ_IIB_FLOW_MAPPING_DELIMETER = "," ;
	public static final String SMQ_IIB_INTEGRATION_NODE_START_PATTERN = "Integration node ";
	public static final String SMQ_IIB_QUEUE_MANAGER_PATTERN = "queue manager";
	public static final String SMQ_IIB_DEFAULT_QUEUE_MANAGER_PATTERN = " with default queue manager ";
	public static final String SMQ_IIB_ACTIVE_MULTI_INSTANCE_QUEUE_MANAGER_PATTERN = " is an active multi-instance or High Availability integration node that is running on queue manager ";
	public static final String SMQ_IIB_ADMIN_URI_START_PATTERN1 = " and administration URI";
	public static final String SMQ_IIB_ADMIN_URI_START_PATTERN2 = ". The administration URI";
	public static final String SMQ_IIB_INTEGRATION_SERVER_PATTERN = "on integration server ";
	public static final String SMQ_IIB_APPLICATION_RUNNING_PATTERN = " is running. (Application ";
	public static final String SMQ_IIB_APPLICATION_STOPPED_PATTERN = " is stopped. (Application ";
	public static final String SMQ_IIB_LIBRARY_PATTERN = ", Library";
	public static final String SMQ_IIB_CSV_PATTERN = "net.apmoller.smq";
	public static final String SMQ_IIB_SOURCE_QM_TAG__PATTERN = "Source";
	public static final String SMQ_IIB_DESTINATION_QM_TAG_PATTERN = "Destination";
	public static final String SMQ_IIB_USESSL_TRUE_PATTERN = "true";
	public static final String SMQ_IIB_USESSL_FALSE_PATTERN = "false";
	

	public static final String SMQ_MQINFO_FILE_EXTENSTION = ".data";
	public static final String SMQ_MQINFO_CLUSTERQM_TYPE = "CLUSQMGR";
	public static final String SMQ_MQ_INFO_QUEUE_MANAGER_DETAILS_START_TAG = "DISPLAY QMGR ALL";
	public static final String SMQ_MQ_INFO_QUEUE_DETAILS_START_TAG = "ALL CLUSINFO";
	public static final String SMQ_MQ_INFO_CHANNEL_DETAILS_START_TAG = "DISPLAY CHL";
	public static final String SMQ_MQ_INFO_CHANNEL_STATUS_START_TAG = "DISPLAY CHS";
	public static final String SMQ_MQ_INFO_CLUSTER_QM_DETAILS_START_TAG = "DISPLAY CLUSQMGR";
	public static final String SMQ_MQ_INFO_CLUSTER_QUEUE_DETAILS_START_TAG = "DISPLAY QC";
	public static final String SMQ_MQ_INFO_PROCESS_STATUS_START_TAG = "DISPLAY PRO";
	public static final String SMQ_MQ_INFO_NAMELIST_DETAILS_START_TAG = "DISPLAY NL";
	public static final String SMQ_MQ_INFO_QUEUE_STATUS_START_TAG = "DISPLAY QSTATUS";
	public static final String SMQ_MQ_INFO_TOPIC_DETAILS_START_TAG = "DISPLAY TOPIC";
	public static final String SMQ_MQ_INFO_LISTERNER_DETAILS_START_TAG = "DISPLAY LISTENER";
	public static final String SMQ_MQ_INFO_END_TAG = "12 : END";

	public static final String SMQ_QUEUE_MANAGER_DETAILS_START_BLOCK = "Display Queue Manager details";
	public static final String SMQ_QUEUE_DETAILS_START_BLOCK = "Display Queue details";
	public static final String SMQ_CHANNEL_DETAILS_START_BLOCK = "Display Channel details";
	public static final String SMQ_CHANNEL_STATUS_START_BLOCK = "Display Channel Status details";
	public static final String SMQ_PROCESS_START_BLOCK = "Display Process details";
	public static final String SMQ_NAMELIST_START_BLOCK = "Display namelist details";
	public static final String SMQ_CLUSQUEUMANAGER_START_BLOCK = "Display Cluster Queue Manager details";
	public static final String SMQ_CLUSQUEUE_START_BLOCK = "Display Queue details";
	public static final String SMQ_QUEUESTATUS_START_BLOCK = "Display queue status details";
	public static final String SMQ_TOPIC_START_BLOCK = "Display topic details";
	public static final String SMQ_LISTENER_START_BLOCK = "Display listener information details";
	

	public static final String SMQ_NOT_STARTER = "NOTSTARTER";
	
	//All Queue manager related kvp

	public static final String OPEN_BRACKET = "(";
	public static final String CLOSING_BRACKET = ")";
	public static final String MQINFO_QMNAME = "QMNAME";
	public static final String MQINFO_MAXMSGL = "MAXMSGL";
	
	public static final String MQINFO_QUEUENAME = "QUEUE";
	public static final String MQINFO_IGNORABLE_QUEUE = "TARGTYPE";
	public static final String MQINFO_IGNORABLE_QUEUE2 = "QUEUE\\)";
	public static final String MQINFO_QUEUETYPE = "TYPE";
	public static final String MQINFO_QUEUEBOTHRESH = "BOTHRESH";
	public static final String MQINFO_QUEUEBOQNAME = "BOQNAME";
	public static final String MQINFO_QUEUETARGET = "TARGET";
	public static final String MQINFO_QUEUEDESCR = "DESCR";
	public static final String MQINFO_QUEUETARGTYPE = "TARGTYPE";
	public static final String MQINFO_CLUSTER = "CLUSTER";
	public static final String MQINFO_CONNAME = "CONNAME";
	public static final String MQINFO_CLUSQMGR = "CLUSQMGR";
	public static final String MQINFO_QCLUSTER = "QCLUSTER";
	public static final String MQINFO_QCLUSTER_DESC = "Alias Queue shared in Cluster";
	public static final String MQINFO_QALIAS = "QALIAS";
	public static final String MQINFO_QLOCALS = "QLOCAL";
	public static final String MQINFO_QALIAS_DESC = "Alias Queue";
	public static final String MQINFO_QLOCALS_DESC = "Local Queue";
	public static final String MQINFO_CLUSQT = "CLUSQT";
	

	public static final String MQINFO_IS_CLUSTER_QUEUE = "Yes";
	public static final String MQINFO_IS_NOT_CLUSTER_QUEUE = "No";
	
	

}
