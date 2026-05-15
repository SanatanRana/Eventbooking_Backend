package com.ranadj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RanadjBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RanadjBackendApplication.class, args);
	}

}
