package com.example._Do.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Global configuration class for REST client instances.
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates and configures a default {@link RestClient} bean.
     * This client is primarily used by services (e.g., AiTaskService) to perform
     * outbound HTTP requests to external APIs like Google Gemini.
     *
     * @return a pre-configured {@link RestClient} instance
     */
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

}
