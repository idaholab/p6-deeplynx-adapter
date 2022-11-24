/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.p6;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

// import com.example.evms.Environment;
// import com.example.evms.P6Logger;

public class P6Adapter  {

	private static final Logger LOGGER = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );

    public static void main(String[] args) {
    	try {
			P6Logger.setup();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	LOGGER.log(Level.INFO, "Starting P6Adapter...");
    	System.out.println("P6Adapter has started. Please see the generated logs in Log.txt");

			ReadActivitiesWrapper readActivitiesWrapper = new ReadActivitiesWrapper();

    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	File file = new File(classLoader.getResource("resources/env.yaml").getFile());
    	ObjectMapper om = new ObjectMapper(new YAMLFactory());

    	try {
			Environment env = om.readValue(file, Environment.class);
			Timer time = new Timer();
			P6Scheduler scheduler = new P6Scheduler(readActivitiesWrapper, env, 1);
			time.schedule(scheduler, 0, env.getTimer());
		} catch (JsonParseException e) {
			System.out.println("JsonParseException. StackTrace: ");
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		} catch (JsonMappingException e) {
			System.out.println("JsonMappingException. StackTrace: ");
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			System.out.println("IOException. StackTrace: ");
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}

    }

}
