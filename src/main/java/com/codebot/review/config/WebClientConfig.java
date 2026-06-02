package com.codebot.review.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final GithubProperties githubProperties;
    private final AnthropicProperties anthropicProperties;

    @Bean
    public WebClient githubWebClient() {
        return WebClient.builder()
                .baseUrl(githubProperties.apiBase())
                .defaultHeader("Authorization", "Bearer " + githubProperties.token())
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public WebClient claudeWebClient() {
        return WebClient.builder()
                .baseUrl(anthropicProperties.apiBase())
                .defaultHeader("x-api-key", anthropicProperties.apiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
