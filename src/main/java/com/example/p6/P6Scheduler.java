package com.example.p6;

import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.p6.Environment;

public class P6Scheduler extends TimerTask {

	private static final Logger LOGGER = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );

	ReadActivitiesWrapper readActivitiesWrapper;
	// ReadRelationshipsWrapper readRelationshipsWrapper;
	Environment env;
	int databaseInstance;

	public P6Scheduler(ReadActivitiesWrapper readActivitiesWrapper, Environment env, int databaseInstance) {
		this.readActivitiesWrapper = readActivitiesWrapper;
		// this.readRelationshipsWrapper = readRelationshipsWrapper;
		this.env = env;
		this.databaseInstance = databaseInstance;
	}

	@Override
	public void run() {
		Date now = new Date();
		LOGGER.log(Level.INFO, "P6Adapter run at " + now);

		P6ServiceResponse response = readActivitiesWrapper.mapActivities(this.env, this.databaseInstance);
		LOGGER.log(Level.INFO, "P6 Service Response: " + response.getMsg());

		// todo: mapRelationships must be after mapActivities; this should probably be refactored a little
		P6ServiceResponse response_rels = readActivitiesWrapper.mapRelationships();
		LOGGER.log(Level.INFO, "P6 Service Response_rels: " + response_rels.getMsg());
	}
}
