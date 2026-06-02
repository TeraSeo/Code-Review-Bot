package com.codebot.review.service;

import com.codebot.review.model.ChangedFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final WebClient githubWebClient;

    public List<ChangedFile> getPullRequestFiles(String repoFullName, int prNumber) {
        return githubWebClient.get()
                .uri("/repos/{repo}/pulls/{pr}/files", repoFullName, prNumber)
                .retrieve()
                .bodyToFlux(ChangedFile.class)
                .filter(f -> f.filename().endsWith(".java"))
                .filter(f -> f.patch() != null && !f.patch().isBlank())
                .collectList()
                .block();
    }

    public void postReview(String repoFullName, int prNumber, String body) {
        Map<String, String> request = Map.of("body", body, "event", "COMMENT");
        githubWebClient.post()
                .uri("/repos/{repo}/pulls/{pr}/reviews", repoFullName, prNumber)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
