package com.example.p6;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.HashMap;
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;
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
		return "OK";
	}

	/**
    * Return the status of the connection to the P6 datasource
    */
	@GetMapping("/status")
	public HashMap<String, String> status() {

		// TODO: add code to test the connection to P6 and return a status

		HashMap<String, String> status_map = new HashMap<String, String>();
		SQLConnect sqlconnect = new SQLConnect();

		if (sqlconnect.connect()) {
			sqlconnect.addLog("GET | /status");
			status_map.put("sql_driver_name", sqlconnect.getDriverName());
			sqlconnect.close();
		}

		return status_map;
	}

	/**
    * Get the post body requirement for the configure POST endpoint
    */
	@GetMapping("/configure") // TODO: delete, probably
	public HashMap<String, String> configure_example() {

		HashMap<String, String> example_map = new HashMap<String, String>();
		SQLConnect sqlconnect = new SQLConnect();

		if (sqlconnect.connect()) {
			sqlconnect.addLog("GET | /configure");
			sqlconnect.close();
		}

		example_map.put("deepLynxURL", "STRING");
		example_map.put("deepLynxContainer", "STRING");
		example_map.put("deepLynxDatasource", "STRING");
		example_map.put("deepLynxApiKey", "STRING");
		example_map.put("deepLynxApiSecret", "STRING");
		example_map.put("p6URL", "STRING");
		example_map.put("p6Project", "STRING");
		example_map.put("p6Username", "STRING");
		example_map.put("p6Password", "STRING");

		return example_map;
	}

	/**
    * Create an entry in the connections table for the adapter to run on
    */
	@PostMapping("/configure")
	public HashMap<String, String> configure(@RequestBody HashMap<String, String> payload) {

		HashMap<String, String> status_map = new HashMap<String, String>();

		SQLConnect sqlconnect = new SQLConnect();
		// status_map.put("sql_migration_success", "false");
		status_map.put("sql_configuration_success", "false");

		if (sqlconnect.connect()) {
			sqlconnect.addLog("POST | /configure");
			// boolean migration_success = sqlconnect.migrate();
			// status_map.put("sql_migration_success", String.valueOf(migration_success));
			boolean configuration_success = sqlconnect.addConnection(payload);
			status_map.put("sql_configuration_success", String.valueOf(configuration_success));
			sqlconnect.close();
		}

		return status_map;
	}

	/**
    * Update the adapter's configuration settings.
    */
	@PostMapping("/update") // TODO: determine if this endpoint is redundant
	public HashMap<String, String> update(@RequestBody HashMap<String, String> payload) {

		HashMap<String, String> status_map = new HashMap<String, String>();

		SQLConnect sqlconnect = new SQLConnect();
		// status_map.put("sql_migration_success", "false");
		status_map.put("sql_configuration_success", "false");

		if (sqlconnect.connect()) {
			sqlconnect.addLog("POST | /update");
			// boolean migration_success = sqlconnect.migrate();
			// status_map.put("sql_migration_success", String.valueOf(migration_success));
			boolean configuration_success = sqlconnect.addConnection(payload);
			status_map.put("sql_configuration_success", String.valueOf(configuration_success));
			sqlconnect.close();
		}

		return status_map;
	}

	/**
    * Starts the adapter run
    */
	@GetMapping("/startAdapter")
	public HashMap<String, String> test() {

		HashMap<String, String> status_map = new HashMap<String, String>();
		try {
			SQLConnect sqlconnect = new SQLConnect();

			if (sqlconnect.connect()) {
				sqlconnect.addLog("POST | /test");
				ArrayList<HashMap<String, String>> connections = sqlconnect.getConnections();
				status_map = connections.get(connections.size() - 1);
				sqlconnect.close();
			}

			try {
			P6Logger.setup();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	LOGGER.log(Level.INFO, "Starting P6Adapter...");
    	System.out.println("P6Adapter has started. Please see the generated logs in Log.txt");

			ReadActivitiesWrapper readActivitiesWrapper = new ReadActivitiesWrapper();

    	try {
			Environment env = new Environment(status_map.get("p6Username"), status_map.get("p6Password"), status_map.get("deepLynxURL"), status_map.get("deepLynxContainer"), status_map.get("deepLynxDatasource"), status_map.get("deepLynxApiKey"), status_map.get("deepLynxApiSecret"), status_map.get("p6URL"), status_map.get("p6Project"), 10000);
			Timer time = new Timer();
			P6Scheduler scheduler = new P6Scheduler(readActivitiesWrapper, env, 1);
			time.schedule(scheduler, 0, env.getTimer());
		} catch (Exception e) {
			System.out.println("Exception. StackTrace: ");
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}

	} catch(Exception e) {
		status_map.put("failed", "true");
	}


		return status_map;
	}

}
