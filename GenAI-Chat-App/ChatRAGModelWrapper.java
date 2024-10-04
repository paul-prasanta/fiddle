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
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.Citation;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrieveAndGenerateConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateInput;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateResponse;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateType;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrievedReference;

@Service
public class ChatRAGModelWrapper {

	private final String MODEL_ARN = "YOUR-MODEL-ARN";
	private final String KNOWLEDGE_BASE_ID = "YOUR-KNOWLEDGE-BASE-ID";
	
	@Autowired
	private BedrockAgentRuntimeClient bedrockAgentClient;
	
	public String process(String query) {
		return invokeModelWithRAG(query);
	}
	
	private String invokeModelWithRAG(String query) {
		System.out.println("RAG - Invoke Knowledge Base and Model");
		
		// Config
		KnowledgeBaseRetrieveAndGenerateConfiguration kbConfig = KnowledgeBaseRetrieveAndGenerateConfiguration.builder()
				.knowledgeBaseId(KNOWLEDGE_BASE_ID)
				.modelArn(MODEL_ARN)
				.build();
		RetrieveAndGenerateConfiguration config = RetrieveAndGenerateConfiguration.builder()
				.knowledgeBaseConfiguration(kbConfig)
				.type(RetrieveAndGenerateType.KNOWLEDGE_BASE) // If not defined (Error: Invalid input provided)
				.build();
		
		// Input
		RetrieveAndGenerateInput input = RetrieveAndGenerateInput.builder().text(query).build();
		
		RetrieveAndGenerateRequest request = RetrieveAndGenerateRequest.builder()
				.input(input)
				.retrieveAndGenerateConfiguration(config)
				.build();
		
		// Response from Knowledge Base
		RetrieveAndGenerateResponse response = bedrockAgentClient.retrieveAndGenerate(request);
		System.out.println("RAG - Response: "+ response.toString());
		System.out.println("RAG - Output: "+ response.output().text());
		System.out.println("RAG - Count of Citations: "+ (response.hasCitations() ? response.citations().size() : 0));
		
		// 1st citation (0th index) is actual result
		for(Citation c : response.citations()) {
			System.out.println("RAG - Retrival References: "+ (c.hasRetrievedReferences() ? c.retrievedReferences().size() : 0));
			
			for(RetrievedReference r : c.retrievedReferences()) {
				System.out.println("RAG - Content: "+ r.content().text());
				System.out.println("RAG - Location: "+ r.location().s3Location().uri());
			}
		}
		return response.output().text(); // Response generated from query
	}
}
