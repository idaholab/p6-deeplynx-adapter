package com.example.p6;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boots!";
	}

	@GetMapping("/health")
	public String health() {
		return "OK";
	}

}