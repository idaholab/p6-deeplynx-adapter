package com.example.p6;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class P6Controller {

	private static final Logger LOGGER = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );

	/**
    * Base endpoint
    */
	@GetMapping("/")
	public String index() {
		SQLConnect sqlconnect = new SQLConnect();

		if (sqlconnect.connect()) {
			sqlconnect.addLog("GET | /");
			sqlconnect.close();
		}
		LOGGER.log(Level.INFO, "/");
		return "P6 adapter for Deep Lynx";
	}

	/**
    * Health query
    */
	@GetMapping("/health")
	public String health() {
		SQLConnect sqlconnect = new SQLConnect();

		if (sqlconnect.connect()) {
			sqlconnect.addLog("GET | /health");
			sqlconnect.close();
		}
		LOGGER.log(Level.INFO, "/health");
		return "OK";
	}

	/**
	* Return the status of the connection to the P6 datasource
	*/
	// todo: need to change how we add extra_hosts in docker-compose file
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

	/**
    * Logs query
    */
	@GetMapping("/logs")
	public HashMap<String, String> logs() throws IOException {
		SQLConnect sqlconnect = new SQLConnect();

		HashMap<String, String> log_map = new HashMap<String, String>();

		if (sqlconnect.connect()) {
			sqlconnect.addLog("GET | /logs");
			sqlconnect.close();
		}

		try (BufferedReader br = new BufferedReader(new FileReader("/filestore/Log.txt"))) {
			String line;

			while ((line = br.readLine()) != null) {
				log_map.put(line, br.readLine());
			}
			br.close();
		}

		return log_map;
	}

	public String p6Status(String p6url) {
		try {
			URL url = new URL(p6url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			String status = Integer.toString(conn.getResponseCode());
			conn.disconnect();
			return status;
		} catch (UnknownHostException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
			return "Unknown Host";
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
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

		if (sqlconnect.connect()) {
			sqlconnect.addLog("POST | /configure");
			boolean configuration_success = sqlconnect.addConnection(payload);
			status_map.put("configuration_success", String.valueOf(configuration_success));
			sqlconnect.close();
		}

		return status_map;
	}

	/**
    * Update an entry in the connections table
    */
	@PutMapping("/update")
	public HashMap<String, String> update(@RequestBody HashMap<String, String> payload) {

		HashMap<String, String> status_map = new HashMap<String, String>();

		SQLConnect sqlconnect = new SQLConnect();
		status_map.put("update_success", "false");

		if (sqlconnect.connect()) {
			sqlconnect.addLog("PUT | /update");
			boolean update_success = sqlconnect.addConnection(payload);
			status_map.put("update_success", String.valueOf(update_success));
			sqlconnect.close();
		}

		return status_map;
	}

	/**
    * Delete an entry in the connections table
    */
	@DeleteMapping("/delete")
	public HashMap<String, String> delete(@RequestBody HashMap<String, String> payload) {

		HashMap<String, String> status_map = new HashMap<String, String>();

		SQLConnect sqlconnect = new SQLConnect();
		status_map.put("delete_success", "false");

		if (sqlconnect.connect()) {
			sqlconnect.addLog("DELETE | /delete");
			boolean delete_success = sqlconnect.deleteConnection(payload);
			status_map.put("delete_success", String.valueOf(delete_success));
			sqlconnect.close();
		}

		return status_map;
	}
}
