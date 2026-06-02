package com.codebot.review.service;

import com.codebot.review.model.ChangedFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
@ExtendWith(MockitoExtension.class)
class GitHubServiceTest {

    @Mock private WebClient githubWebClient;
    @InjectMocks private GitHubService gitHubService;

    @Test
    @DisplayName(".java 파일만 필터링하고 patch 없는 파일 제외")
    void getPullRequestFiles_filtersNonJavaAndNullPatch() {
        var javaFile = new ChangedFile("Main.java", "+ int x;");
        var xmlFile = new ChangedFile("pom.xml", "+ <version>1</version>");
        var noPatchFile = new ChangedFile("Readme.java", null);

        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(uriSpec).when(githubWebClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString(), anyString(), anyInt());
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(Flux.just(javaFile, xmlFile, noPatchFile))
                .when(responseSpec).bodyToFlux(ChangedFile.class);

        List<ChangedFile> result = gitHubService.getPullRequestFiles("owner/repo", 1);

        assertEquals(1, result.size());
        assertEquals("Main.java", result.get(0).filename());
    }

    @Test
    @DisplayName("PR 리뷰 코멘트 정상 등록")
    void postReview_callsGitHubApi() {
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(uriSpec).when(githubWebClient).post();
        doReturn(bodySpec).when(uriSpec).uri(anyString(), anyString(), anyInt());
        doReturn(bodySpec).when(bodySpec).bodyValue(any());
        doReturn(responseSpec).when(bodySpec).retrieve();
        doReturn(Mono.empty()).when(responseSpec).toBodilessEntity();

        gitHubService.postReview("owner/repo", 1, "리뷰 내용");

        verify(githubWebClient).post();
    }
}
