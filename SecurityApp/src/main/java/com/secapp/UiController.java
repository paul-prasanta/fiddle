/*
 * Copyright (C) 2024 Prasanta Paul, https://prasanta-paul.blogspot.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.secapp;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.secapp.config.LoginUtilityService;
import com.secapp.config.Users;
import com.secapp.jwt.JwtUtility;

@RequestMapping("/")
@Controller
public class UiController {

	String SCREEN_LABEL = "Security App - UI [%s]";
	
	@Autowired
	JwtUtility jwtUtility;
	@Autowired
	LoginUtilityService loginUtilityService;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@GetMapping("")
	public String home(Model model) {
		String label = String.format(SCREEN_LABEL, "HOME-PAGE");
		logger.info("Request - " + label);
		model.addAttribute("label", label);
		return "home";
	}
	
	@GetMapping("/open")
	public String openPage(Model model) {
		String label = String.format(SCREEN_LABEL, "OPEN-PAGE");
		logger.info("Request - " + label);
		model.addAttribute("label", label);
		return "home";
	}

	@GetMapping("/protected")
	public String protectedPage(Model model) {
		String label = String.format(SCREEN_LABEL, "PROTECTED-PAGE");
		logger.info("Request - " + label);
		model.addAttribute("label", label);
		
		Users user = loginUtilityService.getLoggedInUser();
		if (user != null) {
			model.addAttribute("userName", user.getUserName());
			
			// Generate JWT Token for API requests
			Map<String, String> extras = new HashMap<String, String>();
			extras.put("data-1", "sample");
			int expiryInterval = 24 * 60 * 60 * 1000;
			
			String token = jwtUtility.generateToken(extras, user.getUserName(), expiryInterval);
			
			logger.info("JWT Token: "+ token);
			
			model.addAttribute("jwtToken", token);
		}
		
		return "home";
	}
}
