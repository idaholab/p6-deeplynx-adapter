package com.example.p6;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.HashMap;


@RestController
public class P6Controller {

	/**
    * Base endpoint
    */
	@GetMapping("/")
	public String index() {
		return "P6 adapter for Deep Lynx";
	}

	/**
    * Health query
    */
	@GetMapping("/health")
	public String health() {
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
			status_map.put("sql_driver_name", sqlconnect.getDriverName());
			sqlconnect.close();
		}

		return status_map;
	}

	/**
    * Get the post body requirement for the configure POST endpoint
    */
	@GetMapping("/configure")
	public HashMap<String, String> configure_example() {

		HashMap<String, String> example_map = new HashMap<String, String>();
			
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
		status_map.put("sql_migration_success", "false");
		status_map.put("sql_configuration_success", "false");

		if (sqlconnect.connect()) {
			boolean migration_success = sqlconnect.migrate();
			status_map.put("sql_migration_success", String.valueOf(migration_success));
			boolean configuration_success = sqlconnect.addConnection(payload);
			status_map.put("sql_configuration_success", String.valueOf(configuration_success));
			sqlconnect.close();
		}

		return status_map;
	}

	/**
    * Update the adapter's configuration settings.
    */
	@PostMapping("/update") // TODO: detemrine if this endpoint is redundant
	public HashMap<String, String> update(@RequestBody HashMap<String, String> payload) {

		HashMap<String, String> status_map = new HashMap<String, String>();

		SQLConnect sqlconnect = new SQLConnect();
		status_map.put("sql_migration_success", "false");
		status_map.put("sql_configuration_success", "false");

		if (sqlconnect.connect()) {
			boolean migration_success = sqlconnect.migrate();
			status_map.put("sql_migration_success", String.valueOf(migration_success));
			boolean configuration_success = sqlconnect.addConnection(payload);
			status_map.put("sql_configuration_success", String.valueOf(configuration_success));
			sqlconnect.close();
		}

		return status_map;
	}

	/**
    * Update the adapter's configuration settings.
    */
	@GetMapping("/test") // TODO: detemrine if this endpoint is redundant
	public HashMap<String, String> test() {

		HashMap<String, String> status_map = new HashMap<String, String>();

		SQLConnect sqlconnect = new SQLConnect();
		status_map.put("sql_migration_success", "false");
		status_map.put("sql_configuration_success", "false");

		if (sqlconnect.connect()) {
			sqlconnect.getConnections();
			sqlconnect.close();
		}

		return status_map;
	}

}