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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class AwsConfig {

	// Define credentails in application.properties file
	@Value("${pingo.cloud.aws.credentials.access-key}")
    private String accessKey;
    @Value("${pingo.cloud.aws.credentials.secret-key}")
    private String secretKey;
    @Value("${pingo.cloud.aws.region.static}")
    private String region;
    
    public AwsCredentialsProvider awsCredentialsProvider() {
        AwsCredentialsProvider awsCredentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));
        return awsCredentialsProvider;
    }
	
	// For Model Access
    @Bean
    public BedrockRuntimeClient bedrockClient() {
        return BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider())
                .build();
    }
    
	// For Model Access through Knowledgebase (RAG)
    @Bean
    public BedrockAgentRuntimeClient bedrockAgentRuntime() {
    	return BedrockAgentRuntimeClient.builder()
    			.region(Region.of(region))
    			.credentialsProvider(awsCredentialsProvider())
    			.build();
    }
}
