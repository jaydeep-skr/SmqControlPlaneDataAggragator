package org.smq.controlplane.db.transaction;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author MalobikaMalkhandi
 *
 */
public class DBTransaction {
	
	public static void listFilesForFolder(final File folder, final String indexName_ES, final String elasticSearchURL) throws IOException, ParseException {
		
		//String elasticSearchURL = "https://quickstart-es-http.default.svc.cluster.local:9200";
		int id=1;
		for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry, indexName_ES, elasticSearchURL);
	        } else {
	            //System.out.println(fileEntry.getName());
	            String filename=fileEntry.getName();
	            Scanner scanner = new Scanner(Paths.get(fileEntry.getAbsolutePath()), StandardCharsets.UTF_8.name());
	            String content = scanner.useDelimiter("\\A").next();
	            scanner.close();
	            //System.out.println(content);
	            JSONParser jsonParser = new JSONParser();
	            JSONArray JSONArray = (JSONArray) jsonParser.parse(content);
	            //System.out.println(JSONArray.get(0)); ////
	            JSONObject json= (JSONObject)JSONArray.get(0);
	            // JSONObject sourceApp = (JSONObject) json.get("sourceApp");
	            // //JSONObject appName = (JSONObject) sourceApp.get("appName");
	            
	            // String[] bits = filename.split("\\.");
	            // String lastToken = bits[bits.length-2];
	            // String[] appNames= lastToken.split("_");
	            
	            // sourceApp.put("appName", appNames[0]);
	            
	            // JSONObject destinationApp = (JSONObject) json.get("destinationApp");
	            // destinationApp.put("appName", appNames[1]);
	            // System.out.println(JSONArray.toJSONString()); ///
	            
	            //String url="https://c47ae109e6a44b3da88cd1e8c8d01d2b.us-central1.gcp.cloud.es.io:9243/smqflows-main/_doc/";
				
	            //String url="https://quickstart-es-http.default.svc.cluster.local:9200/smqflows-main/_doc/";
				String url = elasticSearchURL + "/"+ indexName_ES + "/_doc/";

	            
	            //URL urlForGetRequest = new URL(null, url+id,new sun.net.www.protocol.https.Handler());
	            URL urlForGetRequest = new URL( url+id);
				
				HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
				conection.setRequestMethod("POST");
				conection.setConnectTimeout(20000);
				conection.setRequestProperty("Content-Type", "application/json");
				conection.setRequestProperty("Authorization", "Basic ZWxhc3RpYzoyNGcwMjdUTTZZQlhJeTd5bjVCb3c2OGY=");

				conection.setRequestProperty("Cookie", "bm_sv=; ak_bmsc=");
				 // Send post request 
				conection.setDoOutput(true);
		        try (DataOutputStream wr = new DataOutputStream(conection.getOutputStream())) {
		            wr.writeBytes(json.toJSONString());
		            wr.flush();
		        }
				
				
				int responseCode = conection.getResponseCode();
				
				System.out.println(responseCode);
				conection.disconnect();
				id++;

	        }
	    }
	}

	static {
	    disableSslVerification();
	}

	private static void disableSslVerification() {
	    try
	    {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) {
	            }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
				
	        }
	        };

	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };

	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (KeyManagementException e) {
	        e.printStackTrace();
	    }
	}
	
	//public static void main(String [] args) throws IOException, ParseException {
	//	final File folder = new File("C:\\Malobika\\ElasticSearchPOC\\IIB-flowjson\\json");
	//	listFilesForFolder(folder);
	//}
	
	
}
