package com.example.coupangclone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CoupangcloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoupangcloneApplication.class, args);
	}

}
