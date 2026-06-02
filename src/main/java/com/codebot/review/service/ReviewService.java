package com.codebot.review.service;

import com.codebot.review.model.PullRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final GitHubService gitHubService;
    private final ClaudeService claudeService;

    public void review(PullRequestEvent event) {
        var repo = event.repository().fullName();
        var prNumber = event.pullRequest().number();

        var files = gitHubService.getPullRequestFiles(repo, prNumber);
        if (files == null || files.isEmpty()) return;

        var reviewBody = claudeService.review(files);
        gitHubService.postReview(repo, prNumber, reviewBody);
    }
}
