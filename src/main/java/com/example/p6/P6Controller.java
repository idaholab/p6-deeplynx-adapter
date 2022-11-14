package com.example.p6;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
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
		status_map.put("last_accessed_by", "brennan.harris@inl.gov");
		status_map.put("last_accessed", "2022-11-14T15:57:09");

		return status_map;
	}

	/**
    * Configure the connection of the adapter to targeted data in both the datasource and Deep Lynx
    */
	@GetMapping("/configure")
	public HashMap<String, String> configure() {

		HashMap<String, String> status_map = new HashMap<String, String>();

		SQLConnect sqlconnect = new SQLConnect(); 
		if (sqlconnect.connect()) {
			status_map.put("sql_driver_name", sqlconnect.driverName());
			sqlconnect.close();
		}

		return status_map;
	}

	/**
    * Update the adapter's configuration settings.
    */
	@GetMapping("/update")
	public HashMap<String, String> update() {

		HashMap<String, String> status_map = new HashMap<String, String>();
		status_map.put("last_accessed_by", "brennan.harris@inl.gov");
		status_map.put("last_accessed", "2022-11-14T15:57:09");

		return status_map;
	}

}