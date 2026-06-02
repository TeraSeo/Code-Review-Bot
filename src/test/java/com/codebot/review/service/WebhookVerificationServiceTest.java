package com.codebot.review.service;

import com.codebot.review.config.GithubProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookVerificationServiceTest {

    @Mock
    private GithubProperties githubProperties;

    @InjectMocks
    private WebhookVerificationService verificationService;

    @BeforeEach
    void setUp() {
        when(githubProperties.webhookSecret()).thenReturn("test-secret");
    }

    @Test
    @DisplayName("올바른 서명이면 검증 통과")
    void verify_validSignature_passes() {
        var payload = "{\"action\":\"opened\"}";
        var validSignature = generateSignature(payload, "test-secret");
        assertDoesNotThrow(() -> verificationService.verify(payload, validSignature));
    }

    @Test
    @DisplayName("잘못된 서명이면 401 예외 발생")
    void verify_invalidSignature_throwsUnauthorized() {
        var ex = assertThrows(ResponseStatusException.class,
                () -> verificationService.verify("payload", "sha256=wrong"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    private String generateSignature(String payload, String secret) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            var hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
