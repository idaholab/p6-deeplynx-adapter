package com.example.p6;

import org.json.*;

import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.*;
import org.springframework.web.client.*;
import java.util.*;

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

	private String restExchange(String path, HttpMethod method, HttpHeaders headers, Optional<String> body) {
			try {
					// add authentication header if present
					if (this.token != null) {
							headers.set("Authorization", "Bearer " + this.getToken());
					}
					HttpEntity<String> requestEntity;
					// create requestEntity based on conditions
					if (method == HttpMethod.POST && body.isPresent()) {
							headers.set("Content-Type", "application/json; charset=UTF-8");
							headers.set("Accept", "application/json");
							requestEntity = new HttpEntity<String>(body.get(), headers);
					} else {
							requestEntity = new HttpEntity<String>(headers);
					}

					RestTemplate restTemplate = new RestTemplate();
					ResponseEntity<String> response = restTemplate.exchange(
							path,
							method,
							requestEntity,
							String.class
					);

					if (response.getStatusCode() == HttpStatus.OK) {
							String responseBody = response.getBody();
							return responseBody;
					} else {
							// Handle non-OK status codes gracefully
							throw new HttpClientErrorException(response.getStatusCode(),
											"Error: " + response.getStatusCodeValue() + " - " + response.getBody());
					}
			} catch (HttpClientErrorException e) {
					// Handle HTTP client errors (4xx) separately
					LOGGER.log(Level.SEVERE, "HTTP client error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
					throw e; // Re-throw the exception to let the caller handle it
			} catch (HttpServerErrorException e) {
					// Handle HTTP server errors (5xx) separately if needed
					LOGGER.log(Level.SEVERE, "HTTP server error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
					throw e; // Re-throw the exception to let the caller handle it
			} catch (RestClientException e) {
					// Handle other REST client exceptions (e.g., network issues)
					LOGGER.log(Level.SEVERE, "RestTemplate exchange failed: " + e.getMessage(), e);
					throw e; // Re-throw the exception to let the caller handle it
			}
	}

	public void authenticate() {
			String path = env.getDeepLynxURL() + "/oauth/token";
			HttpHeaders headers = new HttpHeaders();
			headers.set("x-api-key", env.getApiKey());
			headers.set("x-api-secret", env.getApiSecret());
			headers.set("x-api-expiry", "12h");
			try {
				String token = restExchange(path, HttpMethod.GET, headers, Optional.empty());
				// remove quotes on response string..
				token = token.replace("\"", "");
				this.setToken(token);
			} catch (Exception e) {
					LOGGER.log(Level.SEVERE,"exchange failed: " + e.getMessage(), e);
			}
	}

	public List<String> getContainerIds() {
			String path = env.getDeepLynxURL() + "/containers";
			List<String> containerIds = new ArrayList<>();
			// no headers yet, but Bearer token will be added in restExchange; is this dumb..?
			HttpHeaders headers = new HttpHeaders();
			try {
					String response = restExchange(path, HttpMethod.GET, headers, Optional.empty());
					JSONObject obj = new JSONObject(response);
					JSONArray data = obj.getJSONArray("value");
					for (int i = 0; i < data.length(); i++) {
							containerIds.add(data.getJSONObject(i).getString("id"));
					}
			} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "DeepLynxService.getContainerIds() failed: " + e.toString());
			}

			return containerIds;
	}

	public List<HashMap<String, String>> getDatasourceConfigs(List<String> containerIds) {
		List<HashMap<String, String>> configMapList = new ArrayList<HashMap<String, String>>();

		for (int i = 0; i < containerIds.size(); i++) {
			String containerId = containerIds.get(i);
			String path = env.getDeepLynxURL() + "/containers/" + containerId + "/import/datasources?decrypted=true";
			HttpHeaders headers = new HttpHeaders();
			try {
						String datasources = restExchange(path, HttpMethod.GET, headers, Optional.empty());
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

	public void createManualImport(String importBody) {
		try {
				String path = env.getDeepLynxURL() + "/containers/" + env.getContainerId() + "/import/datasources/" + env.getDataSourceId() + "/imports";
				HttpHeaders headers = new HttpHeaders();
				String response = restExchange(path, HttpMethod.POST, headers, Optional.of(importBody));
				JSONObject obj = new JSONObject(response);
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

	// TODO: add better success response
	public void deleteNodes(List<String> p6Ids, String metatype, String nodeIdName) {
		// query
		String urlQuery = env.getDeepLynxURL() + "/containers/" + env.getContainerId() + "/data";
		// TODO: now this code is typemapping dependent..
		// adding params for the two typemapping variables makes this slightly better, still need to discuss
		String body = "{\"query\":\"{\\r\\n    metatypes{\\r\\n        " + metatype + "(\\r\\n            ProjectId: {operator: \\\"eq\\\", value :\\\"" + env.getProjectID() + "\\\"}\\r\\n            _record: {data_source_id: {operator: \\\"eq\\\", value:\\\"" + env.getDataSourceId() + "\\\"}}\\r\\n        ) {\\r\\n            " + nodeIdName + "\\r\\n            _deep_lynx_id\\r\\n        }\\r\\n    }\\r\\n}\\r\\n\",\"variables\":{}}";
		HttpHeaders queryHeaders = new HttpHeaders();
		String queryResponse = restExchange(urlQuery, HttpMethod.POST, queryHeaders, Optional.of(body));
		// System.out.println(queryResponse);
		JSONObject queryObj = new JSONObject(queryResponse);
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
								 HttpHeaders deleteHeaders = new HttpHeaders();
						 		 String deleteResponse = restExchange(urlDelete, HttpMethod.DELETE, deleteHeaders, Optional.empty());
								 System.out.println(deleteResponse);
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
}
