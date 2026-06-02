package com.codebot.review.controller;

import com.codebot.review.service.ReviewService;
import com.codebot.review.service.WebhookVerificationService;
import com.codebot.review.model.PullRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        verificationService.verify(payload, signature);

        if ("pull_request".equals(event)) {
            var prEvent = objectMapper.readValue(payload, PullRequestEvent.class);
            var action = prEvent.action();
            if ("opened".equals(action) || "synchronize".equals(action)) {
                reviewService.review(prEvent);
            }
        }
        return ResponseEntity.ok().build();
    }
}
