package com.example.p6;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class P6Application {

	/**
    * Main entrypoint for application
    */
	public static void main(String[] args) {
		SpringApplication.run(P6Application.class, args);
		SpringApplication.run(Scheduler.class, args);
	}

}
