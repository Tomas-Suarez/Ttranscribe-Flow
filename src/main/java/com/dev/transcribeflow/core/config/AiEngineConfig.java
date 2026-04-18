package com.dev.transcribeflow.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiEngineConfig {

    @Value("${AI_HOST}")
    private String host;

    @Value("${AI_PORT}")
    private String port;


    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    public String getBaseUrl(){
        String baseUrl = host.startsWith("http") ? host : "http://" + host;
        return baseUrl + ":" + port;
    }
}
