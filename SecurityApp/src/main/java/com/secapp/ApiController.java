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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secapp.config.LoginUtilityService;
import com.secapp.config.Users;

@RequestMapping("/api")
@RestController
public class ApiController {

	String SCREEN_LABEL = "Security App - API Home [%s] [%s]";
	
	@Autowired
	LoginUtilityService loginUtilityService;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@GetMapping("/open")
	public String openApi() {
		String label = String.format(SCREEN_LABEL, "OPEN-API", "");
		logger.info("Request - " + label);
		return label;
	}
	
	@GetMapping("/protected")
	public String protectedApi() {
		Users user = loginUtilityService.getLoggedInUser();
		String label = String.format(SCREEN_LABEL, "PROTECTED-API", user.getUserName() + " / " + user.getRole());
		logger.info("Request - " + label);
		return label;
	}
	
}
