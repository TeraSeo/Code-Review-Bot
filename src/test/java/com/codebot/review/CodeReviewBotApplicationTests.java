package com.codebot.review;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "github.token=test-token",
        "github.webhook-secret=test-secret",
        "github.api-base=https://api.github.com",
        "anthropic.api-key=test-key",
        "anthropic.api-base=https://api.anthropic.com",
        "anthropic.model=claude-sonnet-4-20250514"
})
class CodeReviewBotApplicationTests {

    @Test
    void contextLoads() {
    }
}
