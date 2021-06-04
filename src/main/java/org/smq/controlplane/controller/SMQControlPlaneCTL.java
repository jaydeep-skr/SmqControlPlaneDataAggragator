package org.smq.controlplane.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.json.simple.parser.ParseException;
import org.smq.controlplane.db.transaction.DBTransaction;
import org.smq.controlplane.parser.*;

public class SMQControlPlaneCTL {
	public static void main(String[] args) throws IOException, ParseException {
		final Properties properties = new Properties();
		properties.load(new FileInputStream("/home/ace/pp/application.properties"));

		final String iibLogdir = properties.getProperty("ibm.smq.mqsilist.iib.log.dir");
		final String iibExtractDir = properties.getProperty("ibm.smq.iib.flow.extract.csv.dir");
		final String jsondir = properties.getProperty("ibm.smq.iib.flow.json.dir");
		final String mqDumpdir = properties.getProperty("ibm.smq.mqDump.dir");
		final String mqDumpJSONdir = properties.getProperty("ibm.smq.mqInfoJSON.dir");
		final String elasticSearchUrl = properties.getProperty("ibm.smq.elasticSearch.url");

		if (iibLogdir != null && iibExtractDir != null && jsondir != null) {
			IIBLogFileParser parser = new IIBLogFileParser();
			parser.processIIBFLogs(iibLogdir, iibExtractDir, mqDumpdir, mqDumpJSONdir);
			parser.writeToJSONFiles(jsondir);

			final File iibFlowJSONfolder = new File(jsondir);
			DBTransaction.listFilesForFolder(iibFlowJSONfolder, "smqflows-main", elasticSearchUrl);
			final File mqInfoJSONfolder = new File(mqDumpJSONdir);
			DBTransaction.listFilesForFolder(mqInfoJSONfolder, "smqflows-queue", elasticSearchUrl);
			System.out.println("Done!!");
		} else {
			System.out.println("application.properties not set correctly. Please check...");
		}

	}
}
