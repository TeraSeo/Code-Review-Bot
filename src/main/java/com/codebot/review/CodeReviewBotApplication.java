package com.codebot.review;

import com.codebot.review.config.AnthropicProperties;
import com.codebot.review.config.GithubProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({GithubProperties.class, AnthropicProperties.class})
public class CodeReviewBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeReviewBotApplication.class, args);
	}

}
