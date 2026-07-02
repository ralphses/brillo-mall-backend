package com.clickstechnology.Brillo.Mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = "org.springframework.modulith.events.jpa.JpaEventPublicationAutoConfiguration")
public class BrilloMallApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrilloMallApplication.class, args);
	}

}
