package com.example.p6;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

// @Configuration
// @EnableScheduling
@Component
public class Scheduler {

	private static final Logger LOGGER = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );

	// @Value("${fixed-rate.in.milliseconds}")
  //   private static final long interval;

	// @Scheduled(fixedRate = Scheduler.interval)
	// @Scheduled(fixedRate = 3600000)
	@Scheduled(fixedRate = 10000)
	public void adapterLoop() {
		System.out.println("tick");
		try {
			SQLConnect sqlconnect = new SQLConnect();

			if (sqlconnect.connect()) {
				sqlconnect.addLog("START adapter loop");
				ArrayList<HashMap<String, String>> connections = sqlconnect.getConnections();

				// loop over connections
				for (int i = 0; i < connections.size(); i++) {
					HashMap<String, String> connection = connections.get(i);

					ReadActivitiesWrapper readActivitiesWrapper = new ReadActivitiesWrapper();

					Environment env = new Environment(
						connection.get("p6Username")
						,connection.get("p6Password")
						,connection.get("deepLynxURL")
						,connection.get("deepLynxContainerId")
						,connection.get("deepLynxDatasourceId")
						,connection.get("deepLynxApiKey")
						,connection.get("deepLynxApiSecret")
						,connection.get("p6URL")
						,connection.get("p6Project")
					);


					P6ServiceResponse response = readActivitiesWrapper.mapActivities(env, 1);
					LOGGER.log(Level.INFO, "P6 Service Response: " + response.getMsg());
					sqlconnect.addLog("P6 Service Response: " + response.getMsg());

					// todo: mapRelationships must be after mapActivities; this should probably be refactored a little
					P6ServiceResponse response_rels = readActivitiesWrapper.mapRelationships();
					LOGGER.log(Level.INFO, "P6 Service Response_rels: " + response_rels.getMsg());
					sqlconnect.addLog("P6 Service Response_rels: " + response_rels.getMsg());

					P6ServiceResponse response_codes = readActivitiesWrapper.mapActivityCodeAssignments(env);
					LOGGER.log(Level.INFO, "P6 Service Response_codes: " + response_codes.getMsg());
					sqlconnect.addLog("P6 Service Response_codes: " + response_codes.getMsg());
				}
				sqlconnect.close();

			} else {
				System.out.println("Cannot connect to DB");
			}

		} catch (Exception e) {
			System.out.println("Scheduler failed | " + e.toString());
		}


		// System.out.println("HIT");
	}
}
