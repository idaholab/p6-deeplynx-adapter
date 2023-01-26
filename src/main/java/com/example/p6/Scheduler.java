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
	@Scheduled(fixedRate = 120000)
	public void adapterLoop() {
		System.out.println("tick");
		try {

			// get containers for a given url/key/secret
			Environment initialEnv = new Environment(
				 null
				,null
				,System.getenv("DL_URL")
				,null
				,null
				,System.getenv("DL_KEY")
				,System.getenv("DL_SECRET")
				,null
				,null
			);
			DeepLynxService deeplynx = new DeepLynxService(initialEnv);
			deeplynx.authenticate();
			List<String> containerIds = deeplynx.getContainerIds();

			// get datasource configs for a given url/key/secret + given container ids
			List<HashMap<String, String>> datasources = deeplynx.getDatasourceConfigs(containerIds);

			for (int i = 0; i < datasources.size(); i++) {
				LOGGER.log(Level.INFO, "i: " + i);
				try {
					HashMap<String, String> datasource = datasources.get(i);
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
				  LOGGER.log(Level.SEVERE, "Connection index " + i + " failed");
				}

			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Scheduler failed | " + e.toString());
		}
	}
}
