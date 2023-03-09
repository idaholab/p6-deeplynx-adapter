package com.example.p6;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import org.json.*;

// TODO: remove when moving to prod
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Base64;

@RestController
public class P6Controller {

	private static final Logger LOGGER = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );

	/**
    * Base endpoint
    */
	@GetMapping("/")
	public String index() {
		LOGGER.log(Level.INFO, "GET | /");
		return "P6 adapter for Deep Lynx";
	}

	/**
    * Health query
    */
	@GetMapping("/health")
	public String health() {
		LOGGER.log(Level.INFO, "GET | /health");
		return "OK";
	}

	/**
	* Return the status of the connection to the P6 datasource
	*/
	// TODO: need to change how we add extra_hosts in docker-compose file
	@GetMapping("/status")
	public HashMap<String, String> status() {
		HashMap<String, String> status_map = new HashMap<String, String>();
		// get status for P6 dev
		String p6url = "http://p6-dev-mw:7002/p6ws/services/";
		status_map.put(p6url, p6Status(p6url));

		// get status for each unique p6URL from configuration stored in p6.db
		SQLConnect sqlconnect = new SQLConnect();
		if (sqlconnect.connect()) {
			ArrayList<HashMap<String, String>> connections = sqlconnect.getConnections();
			// loop over connections
			for (int i = 0; i < connections.size(); i++) {
				HashMap<String, String> connection = connections.get(i);
				p6url = connection.get("p6URL");
				status_map.put(p6url, p6Status(p6url));
			}
			sqlconnect.close();
		} else {
			LOGGER.log(Level.INFO, "Cannot connect to DB");
		}

		return status_map;
	}

	@GetMapping("/view_db")
	public HashMap<String, String> view_db() {
		HashMap<String, String> view_map = new HashMap<String, String>();
		// get status for each unique p6URL from configuration stored in p6.db
		SQLConnect sqlconnect = new SQLConnect();
		if (sqlconnect.connect()) {
			ArrayList<HashMap<String, String>> connections = sqlconnect.getConnections();
			// loop over connections
			for (int i = 0; i < connections.size(); i++) {
				HashMap<String, String> connection = connections.get(i);
				view_map.put(connection.get("serviceUserKey"), connection.get("serviceUserSecret"));
			}
			sqlconnect.close();
		} else {
			LOGGER.log(Level.INFO, "Cannot connect to DB");
		}

		return view_map;
	}

	/**
    * Logs query
    */
	@GetMapping("/logs")
	public HashMap<Integer, String> logs() throws IOException {
		HashMap<Integer, String> log_map = new HashMap<Integer, String>();

		LOGGER.log(Level.INFO, "GET | /logs");

		try (BufferedReader br = new BufferedReader(new FileReader("/var/app/sqlite/Log.txt"))) {
			String line;

			Integer count = 0;

			while ((line = br.readLine()) != null) {
				log_map.put(count, line);
				count += 1;
			}
			br.close();
		}

		return log_map;
	}

	/**
    * Connection status of P6 instance
    */
	public String p6Status(String p6url) {
		try {
			URL url = new URL(p6url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			String status = Integer.toString(conn.getResponseCode());
			conn.disconnect();
			return status;
		} catch (UnknownHostException e) {
			LOGGER.log(Level.SEVERE,"p6Status failed: " + e.toString(), e);
			return "Unknown Host";
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"p6Status failed: " + e.toString(), e);
			return e.toString();
		}
	}


	/**
    * Create an entry in the connections table for the adapter to run on
    */
	@PostMapping("/configure")
	public HashMap<String, String> configure(@RequestBody HashMap<String, String> payload) {

		HashMap<String, String> status_map = new HashMap<String, String>();

		SQLConnect sqlconnect = new SQLConnect();
		status_map.put("configuration_success", "false");

		LOGGER.log(Level.INFO, "POST | /configure");

		if (sqlconnect.connect()) {
			int rows_affected = sqlconnect.addConnection(payload);
			if (rows_affected > 0) {
				status_map.put("configuration_success", "true");
			}
			status_map.put("rows_affected", String.valueOf(rows_affected));

			sqlconnect.close();
		}

		return status_map;
	}

	/**
    * Update an entry in the connections table
    */
	// @PutMapping("/update")
	// public HashMap<String, String> update(@RequestBody HashMap<String, String> payload) {
	//
	// 	HashMap<String, String> status_map = new HashMap<String, String>();
	//
	// 	SQLConnect sqlconnect = new SQLConnect();
	// 	status_map.put("update_success", "false");
	//
	// 	LOGGER.log(Level.INFO, "POST | /update");
	//
	// 	if (sqlconnect.connect()) {
	// 		int rows_affected = sqlconnect.updateConnection(payload);
	// 		if (rows_affected > 0) {
	// 			status_map.put("update_success", "true");
	// 		}
	// 		status_map.put("rows_affected", String.valueOf(rows_affected));
	// 		sqlconnect.close();
	// 	}
	//
	// 	return status_map;
	// }

	/**
    * Delete an entry in the connections table
    */
	// @DeleteMapping("/delete")
	// public HashMap<String, String> delete(@RequestBody HashMap<String, String> payload) {
	//
	// 	HashMap<String, String> status_map = new HashMap<String, String>();
	//
	// 	SQLConnect sqlconnect = new SQLConnect();
	// 	status_map.put("delete_success", "false");
	//
	// 	LOGGER.log(Level.INFO, "POST | /delete");
	//
	// 	if (sqlconnect.connect()) {
	// 		int rows_affected = sqlconnect.deleteConnection(payload);
	// 		if (rows_affected > 0) {
	// 			status_map.put("delete_success", "true");
	// 		}
	// 		status_map.put("rows_affected", String.valueOf(rows_affected));
	//
	// 		sqlconnect.close();
	// 	}
	//
	// 	return status_map;
	// }

	/**
    * Delete static resources
	* TODO: remove before moving to prod
    */
	@DeleteMapping("/nuke")
	public HashMap<String, String> delete() {

		HashMap<String, String> status_map = new HashMap<String, String>();

		status_map.put("nuke_success", "false");

		LOGGER.log(Level.INFO, "POST | /nuke");

		try {
			FileUtils.cleanDirectory(new File("/var/app/sqlite"));
			// TODO: find a way to recreate the logger
			P6Logger.setup();
			status_map.put("nuke_success", "true");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}


		return status_map;
	}

	/**
    * Create an entry in the connections table for the adapter to run on
    */
	@PostMapping("/add_cert")
	public HashMap<String, String> add_cert(@RequestBody HashMap<String, String> payload) {

		HashMap<String, String> status_map = new HashMap<String, String>();

		SQLConnect sqlconnect = new SQLConnect();
		status_map.put("cert_success", "false");
		LOGGER.log(Level.INFO, "POST | /add_cert");
		try {
			File newFile = new File ("/var/app/lib/dldev.cer");
			// newFile.createNewFile();
			FileUtils.writeStringToFile(newFile, payload.get("cer_string"), "UTF8", false);
			Process p = Runtime.getRuntime().exec("keytool -import -noprompt -trustcacerts -alias dlTrust -file lib/dldev.cer -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit");
			status_map.put("cert_success", "true");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}

		return status_map;
	}

	@GetMapping("/redirect/{containerId}")
	public RedirectView redirect(@PathVariable String containerId) {
			// TODO: where does this get set during deployment?
			String appAddress = "http://localhost:8181";
			// TODO: do I need to do that if/else check that Brennan did in the javascript script?
			// if ( !$page.url.searchParams.has("token"))
			String authAddress = String.format("%s/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=p6_adapter&scope=all",
					System.getenv("DL_URL"), System.getenv("DL_APP_ID"), appAddress + "/exchange/" + containerId);
			return new RedirectView(authAddress);
	}

	@GetMapping("/exchange/{containerId}")
	// TODO: should I do anything with state? can add @PathParam to get state variable
	// TODO: Should this return some type of confirmation message?
	// TODO: should I close the page after this exchange? I think that would require some javascript
	public String exchangeToken(@RequestParam String token, @PathVariable String containerId) {
			// exchange the temporary token for an access token
			String url = System.getenv("DL_URL") + "/oauth/exchange";
			String appAddress = "http://localhost:8181";
			JSONObject requestBody = new JSONObject();
			requestBody.put("client_id", System.getenv("DL_APP_ID"));
			requestBody.put("code", token);
			requestBody.put("grant_type", "authorization_code");
			requestBody.put("redirect_uri", appAddress + "/exchange/" + containerId);
			requestBody.put("client_secret", System.getenv("DL_APP_SECRET"));
			requestBody.put("state", "p6_adapter");

			HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
			RestTemplate restTemplate = new RestTemplate();
			try {
					ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
					if (response.getStatusCode() == HttpStatus.OK) {
							JSONObject resObject = new JSONObject(response.getBody());
							// use long_token to create service user and then create service user key pair and store that in P6 db
							String long_token = resObject.getString("value");
							String serviceUserId = createServiceUser(long_token, containerId);
							setServiceUserPermissions(long_token, containerId, serviceUserId);
							createServiceUserKeyPair(long_token, containerId, serviceUserId);
							return "P6 adapter has successfully authenticated with Deep Lynx; please close this page.";
					} else {
							throw new Exception("Error: " + response.getStatusCodeValue() + " - " + response.getBody());
					}
			} catch (HttpStatusCodeException e) {
					System.out.println(e.getResponseBodyAsString());
					LOGGER.log(Level.SEVERE,"exchange failed: " + e.getResponseBodyAsString(), e);
					return e.getResponseBodyAsString();
			} catch (Exception e) {
					System.out.println(e.getMessage());
					LOGGER.log(Level.SEVERE,"exchange failed: " + e.getMessage(), e);
					return e.getMessage();
			}
	}

	// TODO: is display_name required and how should I get it?
	// TODO: I'm guessing I should set the permissions here.. what about roles.. what else?
	public String createServiceUser(String token, String containerId) {
			String url = System.getenv("DL_URL") + "/containers/" + containerId + "/service-users";

			JSONObject requestBody = new JSONObject();
			requestBody.put("display_name", "Jack-test");

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

			HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
			RestTemplate restTemplate = new RestTemplate();
			try {
					ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
					if (response.getStatusCode() == HttpStatus.OK) {
							JSONObject resObject = new JSONObject(response.getBody());
							String serviceUserId = resObject.getJSONObject("value").getString("id");
							// System.out.println(serviceUserId);
							return serviceUserId;
					} else {
							throw new Exception("Error: " + response.getStatusCodeValue() + " - " + response.getBody());
					}
			} catch (HttpStatusCodeException e) {
					// System.out.println(e.getResponseBodyAsString());
					LOGGER.log(Level.SEVERE,"exchange failed: " + e.getResponseBodyAsString(), e);
					return e.getResponseBodyAsString();
			} catch (Exception e) {
					// System.out.println(e.getMessage());
					LOGGER.log(Level.SEVERE,"exchange failed: " + e.getMessage(), e);
					return e.getMessage();
			}
	}

	public void setServiceUserPermissions(String token, String containerId, String serviceUserId) {
			String url = System.getenv("DL_URL") + "/containers/" + containerId + "/service-users/" + serviceUserId + "/permissions";

			// Set the request headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

			// Set the request body
			Map<String, Object> requestBody = new HashMap<>();
			List<String> containers = Arrays.asList("read");
			List<String> ontology = new ArrayList<>();
			List<String> users = new ArrayList<>();
			List<String> data = Arrays.asList("read", "write");
			requestBody.put("containers", containers);
			requestBody.put("ontology", ontology);
			requestBody.put("users", users);
			requestBody.put("data", data);

			// Build the request entity
			HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

			RestTemplate restTemplate = new RestTemplate();
			try {
					// Send the request
					ResponseEntity<String> response = restTemplate.exchange(
						url,
						HttpMethod.PUT,
						request,
						String.class
					);
					if (response.getStatusCode() != HttpStatus.OK) {
							throw new Exception("Error: " + response.getStatusCodeValue() + " - " + response.getBody());
					}
			} catch (HttpStatusCodeException e) {
					LOGGER.log(Level.SEVERE,"exchange failed: " + e.getResponseBodyAsString(), e);
			} catch (Exception e) {
					LOGGER.log(Level.SEVERE,"exchange failed: " + e.getMessage(), e);
			}
	}

	// TODO: should this return something or can I just add the key/secret to the P6 db from here?
	public void createServiceUserKeyPair(String token, String containerId, String serviceUserId) {
			String url = System.getenv("DL_URL") + "/containers/" + containerId + "/service-users/" + serviceUserId + "/keys";
			JSONObject requestBody = new JSONObject();
			requestBody.put("note", "p6_adapter_auth");

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
			HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
			RestTemplate restTemplate = new RestTemplate();
			try {
					ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
					if (response.getStatusCode() == HttpStatus.OK) {
							JSONObject resObject = new JSONObject(response.getBody());
							String serviceUserKey = resObject.getJSONObject("value").getString("key");
							String serviceUserSecret = resObject.getJSONObject("value").getString("secret_raw");
							// System.out.println(serviceUserKey);
							// System.out.println(serviceUserSecret);
							HashMap<String, String> newServiceUser = new HashMap<String, String>();
							newServiceUser.put("serviceUserId", serviceUserId);
							newServiceUser.put("serviceUserKey", serviceUserKey);
							newServiceUser.put("serviceUserSecret", serviceUserSecret);
							this.configure(newServiceUser);

					} else {
							throw new Exception("Error: " + response.getStatusCodeValue() + " - " + response.getBody());
					}
			} catch (HttpStatusCodeException e) {
					// System.out.println(e.getResponseBodyAsString());
					LOGGER.log(Level.SEVERE,"exchange failed: " + e.getResponseBodyAsString(), e);
			} catch (Exception e) {
					// System.out.println(e.getMessage());
					LOGGER.log(Level.SEVERE,"exchange failed: " + e.getMessage(), e);
			}
	}
}
