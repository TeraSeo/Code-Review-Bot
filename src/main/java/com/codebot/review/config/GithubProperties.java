package com.codebot.review.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github")
public record GithubProperties(
        String token,
        String webhookSecret,
        String apiBase
) {}
