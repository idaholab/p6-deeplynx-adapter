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
	private String token = null;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public DeepLynxService(Environment env) {
		this.env = env;
	}

	public void authenticate() {
		String path = env.getDeepLynxURL() + "/oauth/token";

		// supply api keys and expiry via hashmap
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("x-api-key", env.getApiKey());
		headers.put("x-api-secret", env.getApiSecret());
		// TODO: rethink hardcoding expiry
		headers.put("x-api-expiry", "12h");

		try {
					String token = this.makeHTTPRequest(path, "GET", null, headers);
					token = token.replace("\"", "");
					this.setToken(token);
			} catch(Exception e) {
					LOGGER.log(Level.SEVERE, "DeepLynxService.authenticate() failed: " + e.toString());
			}
	}

	public List<String> getContainerIds() {
		String path = env.getDeepLynxURL() + "/containers";
		List<String> containerIds = new ArrayList<String>();

		try {
					String containers = this.makeHTTPRequest(path, "GET", null, null);
					JSONObject obj = new JSONObject(containers);
					JSONArray data = obj.getJSONArray("value");
					for (int i = 0; i < data.length(); i++) {
						containerIds.add(data.getJSONObject(i).getString("id"));
					}
			} catch(Exception e) {
					LOGGER.log(Level.SEVERE, "DeepLynxService.getContainerIds() failed: " + e.toString());
			}

			return containerIds;
	}

	public List<HashMap<String, String>> getDatasourceConfigs(List<String> containerIds) {
		List<HashMap<String, String>> configMapList = new ArrayList<HashMap<String, String>>();

		for (int i = 0; i < containerIds.size(); i++) {
			String containerId = containerIds.get(i);
			String path = env.getDeepLynxURL() + "/containers/" + containerId + "/import/datasources?decrypted=true";
			try {
						String datasources = this.makeHTTPRequest(path, "GET", null, null);
						JSONObject obj = new JSONObject(datasources);
						JSONArray data = obj.getJSONArray("value");
						for (int j = 0; j < data.length(); j++) {
							if (data.getJSONObject(j).getString("adapter_type").equals("p6") && Boolean.TRUE.equals(data.getJSONObject(j).getBoolean("active"))) {
								HashMap<String, String> configMap = new HashMap<String, String>();
								configMap.put("p6Username", data.getJSONObject(j).getJSONObject("config").getString("username"));
								configMap.put("p6Password", data.getJSONObject(j).getJSONObject("config").getString("password"));
								configMap.put("p6URL", data.getJSONObject(j).getJSONObject("config").getString("endpoint"));
								configMap.put("p6Project", data.getJSONObject(j).getJSONObject("config").getString("projectID"));
								configMap.put("deepLynxURL", env.getDeepLynxURL());
								configMap.put("deepLynxContainerId", containerId);
								configMap.put("deepLynxDatasourceId", data.getJSONObject(j).getString("id"));
								configMap.put("deepLynxApiKey", env.getApiKey());
								configMap.put("deepLynxApiSecret", env.getApiSecret());
								configMapList.add(configMap);
							}
						}

						// return configMapList;
				} catch(Exception e) {
						LOGGER.log(Level.SEVERE, "DeepLynxService.getDatasourceConfigs() failed: " + e.toString());
				}
		}

			return configMapList;
	}

	public void createManualImport(File importFile) {
		try {
				String path = env.getDeepLynxURL() + "/containers/" + env.getContainerId() + "/import/datasources/" + env.getDataSourceId() + "/imports";
				JSONObject obj = this.makeHTTPFileRequest(path, importFile);
				boolean isError = obj.getBoolean("isError");
				if (isError) {
					LOGGER.log(Level.SEVERE, "Error with manual import call");
				} else {
					LOGGER.log(Level.INFO, "Successful manual import to Deep Lynx");
				}
			} catch(Exception e) {
					LOGGER.log(Level.SEVERE, "createManualImport failed: " + e.toString());
			}
	}

	public void deleteNodes(List<String> p6Ids, String metatype, String nodeIdName) {
		// query
		String urlQuery = env.getDeepLynxURL() + "/containers/" + env.getContainerId() + "/data";
		// TODO: now this code is typemapping dependent..
		// adding params for the two typemapping variables makes this slightly better, still need to discuss
		String body = "{\"query\":\"{\\r\\n    metatypes{\\r\\n        " + metatype + "(\\r\\n            ProjectId: {operator: \\\"eq\\\", value :\\\"" + env.getProjectID() + "\\\"}\\r\\n            _record: {data_source_id: {operator: \\\"eq\\\", value:\\\"" + env.getDataSourceId() + "\\\"}}\\r\\n        ) {\\r\\n            " + nodeIdName + "\\r\\n            _deep_lynx_id\\r\\n        }\\r\\n    }\\r\\n}\\r\\n\",\"variables\":{}}";
		JSONObject queryObj = new JSONObject(this.makeHTTPRequest(urlQuery, "POST", body, null));
		try {
				if (queryObj.has("data")) {
					JSONArray queryData = queryObj.getJSONObject("data").getJSONObject("metatypes").getJSONArray(metatype);
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
						 try {
								 LOGGER.log(Level.INFO, "deleteNodes will delete DL nodeId: " + nodeId);
								 urlDelete = env.getDeepLynxURL() + "/containers/" + env.getContainerId() + "/graphs/nodes/" + nodeId;
								 this.makeHTTPRequest(urlDelete, "DELETE", null, null);
						 } catch(Exception e) {
								 LOGGER.log(Level.SEVERE, "deleteNodes failed on DL nodeId: " + nodeId + " with error: " + e.toString());
						 }
					 }
				}
				else {
					LOGGER.log(Level.INFO, "deleteNodes didn't find any preexisting " + metatype + " nodes at " + urlQuery);
				}

		} catch(Exception e) {
				LOGGER.log(Level.SEVERE, "deleteNodes failed: " + e.toString());
		}
	}

	public String makeHTTPRequest(String path, String method, String body, HashMap<String, String> headers) {
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

			return content.toString();

		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE,"makeHTTPRequest failed: " + e.toString(), e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,"makeHTTPRequest failed: " + e.toString(), e);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"makeHTTPRequest failed: " + e.toString(), e);
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

			JSONObject obj = new JSONObject(content.toString());
			return obj;

		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE,"makeHTTPFileRequest failed: " + e.toString(), e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,"makeHTTPFileRequest failed: " + e.toString(), e);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"makeHTTPFileRequest failed: " + e.toString(), e);
		}
		return null;
	}

}
