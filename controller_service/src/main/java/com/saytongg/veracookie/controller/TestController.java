package com.saytongg.veracookie.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController{
	
	@GetMapping("/ping")
	private String ping() {
		return "Hello world!";
	}
}