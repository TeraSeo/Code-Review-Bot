package com.codebot.review.service;

import com.codebot.review.config.AnthropicProperties;
import com.codebot.review.model.ChangedFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(MockitoExtension.class)
class ClaudeServiceTest {

    @Mock private WebClient claudeWebClient;
    @Mock private AnthropicProperties anthropicProperties;
    @InjectMocks private ClaudeService claudeService;

    @Test
    @DisplayName("Claude API 응답에서 텍스트 추출")
    void review_returnsTextFromClaudeResponse() {
        when(anthropicProperties.model()).thenReturn("claude-sonnet-4-20250514");

        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        Map<String, Object> claudeResponse = Map.of(
                "content", List.of(Map.of("text", "NPE 가능성 있어요"))
        );

        doReturn(uriSpec).when(claudeWebClient).post();
        doReturn(bodySpec).when(uriSpec).uri(anyString());
        doReturn(bodySpec).when(bodySpec).bodyValue(any());
        doReturn(responseSpec).when(bodySpec).retrieve();
        doReturn(Mono.just(claudeResponse))
                .when(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));

        var files = List.of(new ChangedFile("Foo.java", "+ String s = null;"));
        String result = claudeService.review(files);

        assertEquals("NPE 가능성 있어요", result);
    }
}
