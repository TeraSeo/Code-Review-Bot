package com.codebot.review.service;

import com.codebot.review.model.ChangedFile;
import com.codebot.review.model.PullRequestEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private GitHubService gitHubService;
    @Mock private ClaudeService claudeService;
    @InjectMocks private ReviewService reviewService;

    @Test
    @DisplayName("정상 흐름: 파일 조회 → Claude 리뷰 → 코멘트 등록")
    void review_normalFlow_postsComment() {
        var event = new PullRequestEvent(
                "opened",
                new PullRequestEvent.PullRequest(42, "Fix bug"),
                new PullRequestEvent.Repository("owner/repo")
        );
        var files = List.of(new ChangedFile("A.java", "+ int x;"));
        var reviewText = "초기화 안 하면 NPE 날 수 있어요";

        when(gitHubService.getPullRequestFiles("owner/repo", 42)).thenReturn(files);
        when(claudeService.review(files)).thenReturn(reviewText);

        reviewService.review(event);

        verify(gitHubService).postReview("owner/repo", 42, reviewText);
    }

    @Test
    @DisplayName("변경된 파일 없으면 Claude 호출 스킵")
    void review_noFiles_skipsClaudeCall() {
        var event = new PullRequestEvent(
                "opened",
                new PullRequestEvent.PullRequest(1, "Empty"),
                new PullRequestEvent.Repository("owner/repo")
        );
        when(gitHubService.getPullRequestFiles(any(), anyInt())).thenReturn(List.of());

        reviewService.review(event);

        verifyNoInteractions(claudeService);
    }

    @Test
    @DisplayName("파일 목록이 null이면 Claude 호출 스킵")
    void review_nullFiles_skipsClaudeCall() {
        var event = new PullRequestEvent(
                "opened",
                new PullRequestEvent.PullRequest(1, "Null case"),
                new PullRequestEvent.Repository("owner/repo")
        );
        when(gitHubService.getPullRequestFiles(any(), anyInt())).thenReturn(null);

        reviewService.review(event);

        verifyNoInteractions(claudeService);
    }
}
