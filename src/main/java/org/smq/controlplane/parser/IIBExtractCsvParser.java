package org.smq.controlplane.parser;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.smq.controlplane.iib.flows.model.SmqIibCsvFlowMapper;

public class IIBExtractCsvParser {
	private Map<String, List<SmqIibCsvFlowMapper>> smqIIBFlowQMMapper = new HashMap<String, List<SmqIibCsvFlowMapper>>();
	
	public void processIIBFExtracts(String iibExtractDir)  {
    	
    	//Stream<File> fileStream = Stream.of(new File(iibExtractDir).listFiles()).filter(file -> !file.isDirectory());
		//Set<String> fileList = fileStream.map(File::getName).collect(Collectors.toSet());

    	File lookupDir = new File(iibExtractDir);
    	String[] list = lookupDir.list(new FilenameFilter() {
    	    @Override
    	    public boolean accept(File dir, String name) {
    	        return name.endsWith(LogParserConstants.SMQ_CSV_FILE_EXTENSION);
    	    }
    	});

		Set<String> fileList = new HashSet<>(Arrays.asList(list));	
		
		fileList.forEach(file -> {
			try {
				readFile(iibExtractDir, file);
				
			} catch (IOException e) {
				System.out.println("Unknown exception occurred while reading the directory " + iibExtractDir + " " + e.getMessage());
				e.printStackTrace();
			}
		});		
    }

public void readFile(String fileDirectory, String file) throws IOException {	
		String filePath = fileDirectory + File.separator + file;
		System.out.println("Reading file >> " + filePath);
		
		List<SmqIibCsvFlowMapper> qmFlowDetlist = new ArrayList<>();
	    try (Stream<String> logStream = Files.lines(Paths.get(filePath)).skip(1)) {
	  
	    	logStream.forEach(x -> {
				try {
					populateList(x, qmFlowDetlist);
				} catch (Exception e) {
					System.out.println("Exception occurred while parsing logger of the IIB file " + e.getMessage());
					e.printStackTrace();
				}
			});
	    	
			createIIBQMMapper(qmFlowDetlist);
			
	    } catch (IOException ex) {
	    	System.out.println("Error occurred while reading the IIB file " + ex.getMessage());
	    	ex.printStackTrace();
			throw ex;
	    }
	}


	public void populateList(String iibExtractorMapping, List<SmqIibCsvFlowMapper> flowDetlist) {
		String [] mqDetails = iibExtractorMapping.split(LogParserConstants.SMQ_IIB_FLOW_MAPPING_DELIMETER);		
		SmqIibCsvFlowMapper iibQMDetails = new SmqIibCsvFlowMapper();
		
		if (mqDetails.length == 8) {
			iibQMDetails.setFlowName(mqDetails[0].trim());
			iibQMDetails.setEnvName(mqDetails[1].trim());
			iibQMDetails.setFlowType(mqDetails[2].trim());
			iibQMDetails.setAppRole(mqDetails[3].trim());
			iibQMDetails.setQueueName(mqDetails[4].trim());
			iibQMDetails.setQueueManagerName(mqDetails[5].trim());
			iibQMDetails.setUseSSL(mqDetails[6].trim());
			iibQMDetails.setAppName(mqDetails[7].trim());	

		} else {
			iibQMDetails.setFlowName(mqDetails[0].trim());
			iibQMDetails.setEnvName(mqDetails[1].trim());
			iibQMDetails.setFlowType(mqDetails[2].trim());
			iibQMDetails.setAppRole(mqDetails[3].trim());
			iibQMDetails.setQueueName(mqDetails[4].trim());
			iibQMDetails.setQueueManagerName(mqDetails[5].trim());
			iibQMDetails.setUseSSL(mqDetails[6].trim());
			iibQMDetails.setAppName("DUMMY");			
		}
		
		flowDetlist.add(iibQMDetails);
	}

	
	public void createIIBQMMapper(List<SmqIibCsvFlowMapper> listFlowMapper) {
		
		//create a map of List with flow nme as the key
		this.smqIIBFlowQMMapper = listFlowMapper.stream().collect(Collectors.groupingBy(SmqIibCsvFlowMapper::getFlowName)); 
	    //this.smqIIBFlowQMMapper.entrySet().stream().forEach(x -> System.out.println(x.getKey()));
	    //this.smqIIBFlowQMMapper.entrySet().stream().filter(f -> f.getKey().equalsIgnoreCase("net.apmoller.smq.feed.eddi2.RKEM_EDDI2_CONT_MOVES_Inbound_IQMD_RQ1104_Feed")).map(Map.Entry::getValue)
	    //		.flatMap(List::stream)
	    //		.collect(Collectors.toList()).forEach(s -> System.out.println(s.getQueueManagerName()));	 
	}	
	
	public Map<String, List<SmqIibCsvFlowMapper>> getIIBFlowMapper() {
		return this.smqIIBFlowQMMapper;		
	}
}