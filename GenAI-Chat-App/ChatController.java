/*
 * Copyright (C) 2024 Prasanta Paul, http://prasanta-paul.blogspot.com
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

package com.pingo.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class ChatController {
	
	@Autowired
	ChatModelWrapper chatService;
	@Autowired
	ChatRAGModelWrapper chatRAGService;
	
	@GetMapping("/chat")
	public ResponseEntity<String> chat(@RequestParam("query") String query) {
		String response = chatService.process(query);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	@GetMapping("/chat_with_rag")
	public ResponseEntity<String> chatWithRAG(@RequestParam("query") String query) {
		String response = chatRAGService.process(query);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
}
