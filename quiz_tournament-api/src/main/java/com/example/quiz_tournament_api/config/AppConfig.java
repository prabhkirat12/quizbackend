package com.example.quiz_tournament_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(retryInterceptor()));
        return restTemplate;
    }

    private ClientHttpRequestInterceptor retryInterceptor() {
        return (request, body, execution) -> {
            int maxRetries = 3;
            int retryCount = 0;
            int delay = 2000; // 2 seconds delay

            while (retryCount < maxRetries) {
                try {
                    ClientHttpResponse response = execution.execute(request, body);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return response;
                    } else if (response.getStatusCode().value() == 429) {
                        Thread.sleep(delay);
                        retryCount++;
                    } else {
                        return response;
                    }
                } catch (IOException | InterruptedException e) {
                    if (retryCount >= maxRetries - 1) {
                        throw new RuntimeException("Max retry limit exceeded.", e);
                    }
                    retryCount++;
                }
            }
            throw new RuntimeException("Failed to execute request after retries.");
        };
    }
}
