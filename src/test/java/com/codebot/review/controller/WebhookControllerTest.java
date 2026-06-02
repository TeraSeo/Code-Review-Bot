package com.codebot.review.controller;

import com.codebot.review.service.ReviewService;
import com.codebot.review.service.WebhookVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock private WebhookVerificationService verificationService;
    @Mock private ReviewService reviewService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var objectMapper = new ObjectMapper();
        var controller = new WebhookController(verificationService, reviewService, objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new StringHttpMessageConverter())
                .build();
    }

    @Test
    @DisplayName("PR opened 이벤트 수신 시 200 반환")
    void webhook_prOpenedEvent_returns200() throws Exception {
        var payload = """
                {"action":"opened",
                 "pull_request":{"number":1,"title":"Fix"},
                 "repository":{"full_name":"owner/repo"}}
                """;

        mockMvc.perform(post("/webhook")
                        .header("X-Hub-Signature-256", "sha256=test")
                        .header("X-GitHub-Event", "pull_request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verify(reviewService).review(any());
    }

    @Test
    @DisplayName("PR synchronize 이벤트도 리뷰 실행")
    void webhook_prSynchronizeEvent_triggersReview() throws Exception {
        var payload = """
                {"action":"synchronize",
                 "pull_request":{"number":2,"title":"Update"},
                 "repository":{"full_name":"owner/repo"}}
                """;

        mockMvc.perform(post("/webhook")
                        .header("X-Hub-Signature-256", "sha256=test")
                        .header("X-GitHub-Event", "pull_request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verify(reviewService).review(any());
    }

    @Test
    @DisplayName("PR 외 이벤트는 리뷰 스킵")
    void webhook_nonPrEvent_skipsReview() throws Exception {
        mockMvc.perform(post("/webhook")
                        .header("X-Hub-Signature-256", "sha256=test")
                        .header("X-GitHub-Event", "push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verifyNoInteractions(reviewService);
    }

    @Test
    @DisplayName("closed action은 리뷰 스킵")
    void webhook_prClosedAction_skipsReview() throws Exception {
        var payload = """
                {"action":"closed",
                 "pull_request":{"number":3,"title":"Done"},
                 "repository":{"full_name":"owner/repo"}}
                """;

        mockMvc.perform(post("/webhook")
                        .header("X-Hub-Signature-256", "sha256=test")
                        .header("X-GitHub-Event", "pull_request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verifyNoInteractions(reviewService);
    }
}
