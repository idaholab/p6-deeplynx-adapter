package com.example.p6;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

// @Configuration
// @EnableScheduling
@Component
public class Scheduler {

	private static final Logger LOGGER = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );

	// @Value("${fixed-rate.in.milliseconds}")
  //   private static final long interval;

	// @Scheduled(fixedRate = Scheduler.interval)
	// @Scheduled(fixedRate = 3600000)
	@Scheduled(fixedRate = 86400000)
	public void adapterLoop() {
		System.out.println("tick");

		// get each unique DL (p6 adapter) app service user from configuration stored in p6.db
		SQLConnect sqlconnect = new SQLConnect();
		if (sqlconnect.connect()) {
			ArrayList<HashMap<String, String>> connections = sqlconnect.getConnections();
			// loop over connections
			for (int i = 0; i < connections.size(); i++) {
				HashMap<String, String> connection = connections.get(i);
				try {
					// get containers for a given url/key/secret
					Environment initialEnv = new Environment(
						 null
						,null
						,System.getenv("DL_URL")
						,null
						,null
						,connection.get("serviceUserKey")
						,connection.get("serviceUserSecret")
						,null
						,null
					);
					DeepLynxService deeplynx = new DeepLynxService(initialEnv);
					deeplynx.authenticate();
					// TODO: shouldn't need to get containerIds like this anymore - DL should send containerId to exchangeToken where it can be used in createServiceUser 
					List<String> containerIds = deeplynx.getContainerIds();

					// get datasource configs for a given url/key/secret + given container ids
					List<HashMap<String, String>> datasources = deeplynx.getDatasourceConfigs(containerIds);

					for (int j = 0; j < datasources.size(); j++) {
						LOGGER.log(Level.INFO, "j: " + j);
						try {
							HashMap<String, String> datasource = datasources.get(j);
							ReadActivitiesWrapper readActivitiesWrapper = new ReadActivitiesWrapper();
							Environment env = new Environment(
								 datasource.get("p6Username")
								,datasource.get("p6Password")
								,datasource.get("deepLynxURL")
								,datasource.get("deepLynxContainerId")
								,datasource.get("deepLynxDatasourceId")
								,datasource.get("deepLynxApiKey")
								,datasource.get("deepLynxApiSecret")
								,datasource.get("p6URL")
								,datasource.get("p6Project")
							);

							readActivitiesWrapper.importP6Data(env, 1);

						} catch (Exception e) {
						  LOGGER.log(Level.SEVERE, "Connection index j:" + j + " failed");
						}

					}

				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Scheduler failed | " + e.toString());
				}
			}
			sqlconnect.close();
		} else {
			LOGGER.log(Level.INFO, "Cannot connect to DB");
		}

	}
}
