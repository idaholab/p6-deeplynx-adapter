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
	@Scheduled(fixedRate = 60000)
	public void adapterLoop() {
		System.out.println("tick");
		try {
			SQLConnect sqlconnect = new SQLConnect();

			if (sqlconnect.connect()) {
				LOGGER.log(Level.INFO, "START LOOP");
				ArrayList<HashMap<String, String>> connections = sqlconnect.getConnections();

				// loop over connections
				for (int i = 0; i < connections.size(); i++) {
					try {
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

						readActivitiesWrapper.importP6Data(env, 1);

					} catch (Exception e) {
					  LOGGER.log(Level.SEVERE, "Connection index " + i + " failed");
					}

				}
				sqlconnect.close();
				LOGGER.log(Level.INFO, "END LOOP");

			} else {
				System.out.println("Cannot connect to DB");
				LOGGER.log(Level.SEVERE, "Cannot connect to DB");
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Scheduler failed | " + e.toString());
		}
	}
}
