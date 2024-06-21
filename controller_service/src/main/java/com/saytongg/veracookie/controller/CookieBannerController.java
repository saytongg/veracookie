package com.saytongg.veracookie.controller;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saytongg.veracookie.entity.CookieBannerRecord;
import com.saytongg.veracookie.service.CookieBannerService;

@RestController
public class CookieBannerController{
	@Autowired
	private CookieBannerService cookieBannerService;

	private static final Logger logger = LoggerFactory.getLogger(CookieBannerController.class);
	
	@PostMapping(value = "/analyze")
	@CrossOrigin(origins = "*")
	private ResponseEntity<String> analyzeLink(@RequestParam(name = "link") String link) {

		// Validate given link
		if(!cookieBannerService.validateLink(link)){
			logger.error(String.format("Link %s is in invalid format.", link));
			return ResponseEntity.badRequest().body("The provided link is in invalid format.");
		}

		try{
			CookieBannerRecord record = cookieBannerService.getRatings(link);

			// Build response for the frontend
			final Map<String, String> responseBody = new HashMap<>();
			responseBody.put("image", record.getImage());
			responseBody.put("textRating", record.getTextRating());
			responseBody.put("imageRating", record.getImageRating());

			final ObjectMapper mapper = new ObjectMapper();
			return ResponseEntity.ok(mapper.writeValueAsString(responseBody));
		}
		catch(TimeoutException e){
			logger.error(String.format("Connection to %s timed out.", link));
			return ResponseEntity.internalServerError().body("Cannot connect to the given link. Please try again later.");
		}
		catch(Exception e){
			logger.error(String.format("While analyzing %s, the following error occured: %s", link, e.getMessage()));
			return ResponseEntity.internalServerError().body("Internal server error. Please try again later.");
		}
	}
}