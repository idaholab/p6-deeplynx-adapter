package com.example.p6;

import org.json.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;

public class DeepLynxService {

	private static final Logger LOGGER = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );
	private Environment env;
	private String containerID;
	private String dataSourceID;
	private String apiKey = null;
	private String apiSecret = null;
	private String token = null;
	private String projectID;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public DeepLynxService(Environment env) {
		this.env = env;
		this.apiKey = env.getApiKey();
		this.apiSecret = env.getApiSecret();
		this.containerID = env.getContainerId();
		this.dataSourceID = env.getDataSourceId();
		this.projectID = env.getProjectID();
	}

	public void authenticate() {
		String path = env.getDeepLynxURL() + "/oauth/token";

		// supply api keys and expiry via hashmap
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("x-api-key", this.apiKey);
		headers.put("x-api-secret", this.apiSecret);
		headers.put("x-api-expiry", "12h");

		String token = this.makeGETRequest(path, "GET", null, headers);
		this.setToken(token);
	}

	public void createManualImport(File importFile) {
		String path = env.getDeepLynxURL() + "/containers/" + this.containerID + "/import/datasources/" + this.dataSourceID + "/imports";
		JSONObject obj = this.makeHTTPFileRequest(path, importFile);
		boolean isError = obj.getBoolean("isError");
		if (isError) {
			LOGGER.log(Level.SEVERE, "Error with manual import call");
		} else {
			LOGGER.log(Level.INFO, "Successful manual import to Deep Lynx");
		}
	}

	public void deleteNodes(List<String> p6Ids) {
		// query
		String urlQuery = env.getDeepLynxURL() + "/containers/" + this.containerID + "/data";
		// todo: now this code is typemapping dependent..
		String body = "{\"query\":\"{\\r\\n    metatypes{\\r\\n        Activity(\\r\\n            ProjectId: {operator: \\\"eq\\\", value :\\\"" + this.projectID + "\\\"}\\r\\n            _record: {data_source_id: {operator: \\\"eq\\\", value:\\\"" + this.dataSourceID + "\\\"}}\\r\\n        ) {\\r\\n            Id\\r\\n            _deep_lynx_id\\r\\n        }\\r\\n    }\\r\\n}\\r\\n\",\"variables\":{}}";
		JSONObject queryObj = this.makeHTTPRequest(urlQuery, "POST", body, null);
		JSONArray queryData = queryObj.getJSONObject("data").getJSONObject("metatypes").getJSONArray("Activity");
		// process
		String activityIdDL;
		List<String> nodesToDelete = new ArrayList<String>();
		for (int i = 0; i < queryData.length(); i++) {
			activityIdDL = queryData.getJSONObject(i).getString("Id");
			// check if activity Id is in DL that is no longer in P6
			if (!p6Ids.contains(activityIdDL)) {
				nodesToDelete.add(queryData.getJSONObject(i).getString("_deep_lynx_id"));
			}
		}
		// delete
		String urlDelete;
		 for (String nodeId : nodesToDelete) {
			 urlDelete = env.getDeepLynxURL() + "/containers/" + this.containerID + "/graphs/nodes/" + nodeId;
			 this.makeHTTPRequest(urlDelete, "DELETE", null, null);
		 }
	}

	public JSONObject makeHTTPRequest(String path, String method, String body, HashMap<String, String> headers) {
		URL url;
		HttpURLConnection con;
		try {
			url = new URL(path);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod(method);

			// add authentication header if present
			if (this.token != null) {
				con.setRequestProperty("Authorization", "Bearer " + this.getToken());
			}

			// add user-defined headers following key value pairs
			if (headers != null && headers.size() > 0) {
				for (String i : headers.keySet()) {
					con.setRequestProperty(i, headers.get(i));
				}
			}

			if (method.equals("POST")) {
				con.setDoOutput(true);
				con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				con.setRequestProperty("Accept", "application/json");

				con.connect();
				try(OutputStream os = con.getOutputStream()) {
				    byte[] input = body.getBytes(StandardCharsets.UTF_8);
				    os.write(input, 0, input.length);
				}
			}

			int status = con.getResponseCode();
			LOGGER.log(Level.INFO, path + " status: " + status);
			// If response code != 2XX, return null and handle accordingly
			if (!Integer.toString(status).matches("^2\\S*")) {
				LOGGER.log(Level.SEVERE, "Error with API call");
				return null;
			}

			BufferedReader in = new BufferedReader(
			  new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			in.close();
			con.disconnect();

			LOGGER.log(Level.INFO, content.toString());
			JSONObject obj = new JSONObject(content.toString());
			return obj;

		} catch (MalformedURLException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		return null;
	}

	public String makeGETRequest(String path, String method, String body, HashMap<String, String> headers) {
		URL url;
		HttpURLConnection con;
		try {
			url = new URL(path);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod(method);

			// add authentication header if present
			if (this.token != null) {
				con.setRequestProperty("Authorization", "Bearer " + this.getToken());
			}

			// add user-defined headers following key value pairs
			for (String i : headers.keySet()) {
				con.setRequestProperty(i, headers.get(i));
			}

			int status = con.getResponseCode();
			LOGGER.log(Level.INFO, path + " status: " + status);
			// If response code != 2XX, return null and handle accordingly
			if (!Integer.toString(status).matches("^2\\S*")) {
				LOGGER.log(Level.SEVERE, "Error with API call");
				return null;
			}

			BufferedReader in = new BufferedReader(
			  new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			in.close();
			con.disconnect();

			String returnContent = content.toString();
			returnContent = returnContent.replace("\"", "");
			return returnContent;

		} catch (MalformedURLException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		return null;
	}

	public JSONObject makeHTTPFileRequest(String path, File myFile) {
		URL url;
		HttpURLConnection con;
		try {
			String CRLF = "\r\n";
			String charset = "UTF-8";

			url = new URL(path);
			con = (HttpURLConnection) url.openConnection();

			con.setDoOutput(true);
			String boundary = UUID.randomUUID().toString();
			con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			// add authentication header if present
			if (token != null) {
				con.setRequestProperty("Authorization", "Bearer " + getToken());
			}

			OutputStream output = con.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

			// https://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
			// Send text file.
	    writer.append("--" + boundary).append(CRLF);
	    writer.append("Content-Disposition: form-data; name=\"myFile\"; filename=\"" + myFile.getName() + "\"").append(CRLF);
			// Text file itself must be saved in this charset!
			writer.append("Content-Type: application/json; charset=" + charset).append(CRLF);
	    writer.append(CRLF).flush();
	    Files.copy(myFile.toPath(), output);
			// Important before continuing with writer!
	    output.flush();
			// CRLF is important! It indicates end of boundary.
	    writer.append(CRLF).flush();
	    // End of multipart/form-data.
	    writer.append("--" + boundary + "--").append(CRLF).flush();

			int status = con.getResponseCode();
			LOGGER.log(Level.INFO, path + " status: " + status);
			// If response code != 2XX, return null and handle accordingly
			if (!Integer.toString(status).matches("^2\\S*")) {
				LOGGER.log(Level.SEVERE, "Error with API call");
				return null;
			}

			BufferedReader in = new BufferedReader(
			  new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			in.close();
			con.disconnect();

			LOGGER.log(Level.INFO, content.toString());
			JSONObject obj = new JSONObject(content.toString());
			return obj;

		} catch (MalformedURLException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		return null;
	}

}
