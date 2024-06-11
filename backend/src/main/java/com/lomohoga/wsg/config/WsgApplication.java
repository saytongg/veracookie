package com.lomohoga.wsg.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RedisConfigruation.class)
@ComponentScan("com.lomohoga.wsg.controller")
public class WsgApplication {

	public static void main(String[] args) {
		SpringApplication.run(WsgApplication.class, args);
	}

}
