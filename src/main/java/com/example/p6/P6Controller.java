package com.example.p6;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
		HashMap<String, String> log_map = new HashMap<String, String>();

		LOGGER.log(Level.INFO, "GET | /logs");

		try (BufferedReader br = new BufferedReader(new FileReader("/var/app/sqlite/Log.txt"))) {
			String line;

			int count = 0;

			while ((line = br.readLine()) != null) {
				log_map.put(String.valueOf(count), line);
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
	@PutMapping("/update")
	public HashMap<String, String> update(@RequestBody HashMap<String, String> payload) {

		HashMap<String, String> status_map = new HashMap<String, String>();

		SQLConnect sqlconnect = new SQLConnect();
		status_map.put("update_success", "false");

		LOGGER.log(Level.INFO, "POST | /update");

		if (sqlconnect.connect()) {
			int rows_affected = sqlconnect.updateConnection(payload);
			if (rows_affected > 0) {
				status_map.put("update_success", "true");
			}
			status_map.put("rows_affected", String.valueOf(rows_affected));
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

		LOGGER.log(Level.INFO, "POST | /delete");

		if (sqlconnect.connect()) {
			int rows_affected = sqlconnect.deleteConnection(payload);
			if (rows_affected > 0) {
				status_map.put("delete_success", "true");
			}
			status_map.put("rows_affected", String.valueOf(rows_affected));

			sqlconnect.close();
		}

		return status_map;
	}

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
}
