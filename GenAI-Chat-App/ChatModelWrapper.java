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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Service
public class ChatModelWrapper {

	// Amazon Bedrock - Base Models - Select your model and copy "Model ID" 
	private final String MODEL_ID = "anthropic.claude-3-haiku-20240307-v1:0";
	private final String QUERY_PATTERN = "Human: %s\n\nAssistant:";
	
	@Autowired
    private BedrockRuntimeClient bedrockClient;
	
	public String process(String query) {
		String encodedQuery = String.format(QUERY_PATTERN, query);
		return invokeModel(encodedQuery);
	}
	
	private String invokeModel(String query) {
		// Create Model Payload
		JSONObject obj = new JSONObject();
		obj.put("anthropic_version", "bedrock-2023-05-31") // Refer AWS document
		   .put("max_tokens", 200) // Max Response Size
		   .put("temperature", 0.5) // Randomness of Response (max 1.0)
		   .put("stop_sequences", List.of("\n\nHuman:")); // Char sequences to mark end of query

		JSONObject message = new JSONObject().put("role", "user"); // "user" role
		JSONObject prompt = new JSONObject().put("type", "text").put("text", query); // Query
		message = message.put("content", List.of(prompt));
		obj = obj.put("messages", List.of(message));
		
		String payload = obj.toString();
		System.out.println("__payload: " + obj.toString());
		
		// Invoke Model
		InvokeModelRequest request = InvokeModelRequest.builder().body(SdkBytes.fromUtf8String(payload))
				.modelId(MODEL_ID)
				.contentType("application/json")
				.accept("application/json").build();

		InvokeModelResponse response = bedrockClient.invokeModel(request);
		JSONObject responseBody = new JSONObject(response.body().asUtf8String());
		
		System.out.println("__response: "+ responseBody.toString());
		// Parse response object
		JSONArray contentArray = responseBody.getJSONArray("content");
		String reponseMessage = null;
		if(!contentArray.isEmpty())
			reponseMessage = contentArray.getJSONObject(0).getString("text");
		
		return reponseMessage;
	}
}
