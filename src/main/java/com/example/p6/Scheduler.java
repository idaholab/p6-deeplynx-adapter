package com.example.p6;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {

	// private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	// private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Scheduled(fixedRate = parseInt(System.getenv("P6_EXECUTION_INTERVAL")))
	public void reportCurrentTime() {
		System.out.println("HIT");
	}
}