package com.codebot.review.controller;

import com.codebot.review.service.ReviewService;
import com.codebot.review.service.WebhookVerificationService;
import com.codebot.review.model.PullRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookVerificationService verificationService;
    private final ReviewService reviewService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String payload) throws JsonProcessingException {

        log.info("Webhook received. event={}, signature={}", event, signature);

        try {
            verificationService.verify(payload, signature);
            log.info("Signature verified OK");
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            throw e;
        }

        if ("pull_request".equals(event)) {
            var prEvent = objectMapper.readValue(payload, PullRequestEvent.class);
            var action = prEvent.action();
            log.info("PR event action={}", action);
            if ("opened".equals(action) || "synchronize".equals(action)) {
                reviewService.review(prEvent);
            }
        }
        return ResponseEntity.ok().build();
    }
}
