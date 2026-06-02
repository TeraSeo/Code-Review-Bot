package com.codebot.review.service;

import com.codebot.review.config.AnthropicProperties;
import com.codebot.review.model.ChangedFile;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClaudeService {

    private final WebClient claudeWebClient;
    private final AnthropicProperties anthropicProperties;

    public String review(List<ChangedFile> files) {
        String prompt = buildPrompt(files);

        Map<String, Object> request = Map.of(
                "model", anthropicProperties.model(),
                "max_tokens", 1024,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        Map<String, Object> response = claudeWebClient.post()
                .uri("/v1/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        @SuppressWarnings("unchecked")
        var content = (List<Map<String, Object>>) response.get("content");
        return (String) content.get(0).get("text");
    }

    private String buildPrompt(List<ChangedFile> files) {
        var sb = new StringBuilder();

        /*
         * 핵심 지시: 실제 개발자가 직접 작성한 것처럼 자연스럽게 쓰도록 유도.
         * - 카테고리 태그([버그], [성능] 등) 금지
         * - 지나치게 정중하거나 격식체 금지
         * - 모든 항목 나열하지 않고 실제로 중요한 것만
         * - 짧고 직접적인 피드백
         */
        sb.append("너는 5년차 Java/Spring Boot 백엔드 개발자야. 팀 동료가 올린 PR을 보고 코드 리뷰 코멘트를 남겨줘.\n\n");
        sb.append("규칙:\n");
        sb.append("- 실제로 문제가 있거나 개선이 필요한 부분만 언급해. 칭찬이나 총평은 쓰지 마.\n");
        sb.append("- 카테고리 태그나 번호 매기기 하지 마. 그냥 자연스럽게 써.\n");
        sb.append("- 너무 격식 차리지 말고 팀 내 코드리뷰하듯 캐주얼하게 써.\n");
        sb.append("- 코드 수정 제안은 인라인 코드 블록으로 보여줘.\n");
        sb.append("- 지적이 없으면 \"특이사항 없음\"이라고만 써.\n\n");

        for (var file : files) {
            sb.append("파일: ").append(file.filename()).append("\n");
            sb.append("```diff\n").append(file.patch()).append("\n```\n\n");
        }
        return sb.toString();
    }
}
