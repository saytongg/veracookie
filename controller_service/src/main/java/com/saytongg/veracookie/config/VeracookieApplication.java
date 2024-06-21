package com.saytongg.veracookie.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RedisConfigruation.class)
@ComponentScan({"com.saytongg.veracookie.controller", "com.saytongg.veracookie.service"})
public class VeracookieApplication {

	public static void main(String[] args) {
		SpringApplication.run(VeracookieApplication.class, args);
	}

}
