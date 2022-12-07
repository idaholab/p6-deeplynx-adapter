package com.example.p6;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

		// TODO Jack: add code to test the connection to P6 and return a status

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
