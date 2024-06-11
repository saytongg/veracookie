package com.lomohoga.wsg.controller;

import java.util.HashMap;
import java.util.List;
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
import com.lomohoga.wsg.entity.CookieBannerRecord;
import com.lomohoga.wsg.repository.CookieBannerRecordRepository;
import com.lomohoga.wsg.util.CookieBannerUtil;

@RestController
public class CookieBannerExtractor{
	private static final Logger logger = LoggerFactory.getLogger(CookieBannerExtractor.class);

	@Autowired
	CookieBannerRecordRepository cache;
	
	@PostMapping(value = "/analyze")
	@CrossOrigin(origins = "*")
	private ResponseEntity<String> analyzeLink(@RequestParam(name = "link") String link) {

		// Validate given link
		if(!CookieBannerUtil.validateLink(link)){
			logger.error(String.format("Link %s is in invalid format.", link));
			return ResponseEntity.badRequest().body("The provided link is in invalid format.");
		}

		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, String> responseBody = new HashMap<>();

		try{
			final String baseLink = CookieBannerUtil.getBaseLink(link);
			CookieBannerRecord record = null;

			// See if there is a record from cache
			final List<CookieBannerRecord> records = cache.findByLink(baseLink);

			if(!records.isEmpty()){
				record = records.get(0);
				logger.info(String.format("Result for link %s is obtained from cache.", link));
			}
			else{
				record = CookieBannerUtil.getRatings(baseLink);
				cache.save(record);
			}

			responseBody.put("image", record.getImage());
			responseBody.put("textRating", record.getTextRating());
			responseBody.put("imageRating", record.getImageRating());

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