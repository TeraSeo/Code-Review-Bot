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
        String[] parts = repoFullName.split("/");
        return githubWebClient.get()
                .uri("/repos/{owner}/{repo}/pulls/{pr}/files", parts[0], parts[1], prNumber)
                .retrieve()
                .bodyToFlux(ChangedFile.class)
                .filter(f -> f.filename().endsWith(".java"))
                .filter(f -> f.patch() != null && !f.patch().isBlank())
                .collectList()
                .block();
    }

    public void postReview(String repoFullName, int prNumber, String body) {
        String[] parts = repoFullName.split("/");
        Map<String, String> request = Map.of("body", body, "event", "COMMENT");
        githubWebClient.post()
                .uri("/repos/{owner}/{repo}/pulls/{pr}/reviews", parts[0], parts[1], prNumber)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
