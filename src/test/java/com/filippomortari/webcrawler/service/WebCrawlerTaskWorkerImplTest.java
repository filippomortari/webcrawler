package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerTask;
import com.filippomortari.webcrawler.domain.WebCrawlerVisitedUrl;
import com.filippomortari.webcrawler.domain.repository.WebCrawlerJobExecutionRepository;
import com.filippomortari.webcrawler.domain.repository.WebCrawlerVisitedUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebCrawlerTaskWorkerImplTest {

    private WebCrawlerTaskWorker underTest;
    @Mock
    private WebCrawlerJobExecutionRepository webCrawlerJobExecutionRepository;
    @Mock
    private WebCrawlerVisitedUrlRepository webCrawlerVisitedUrlRepository;
    @Mock
    private UrlVisitor urlVisitor;
    @Mock
    private WebCrawlerTaskDispatcher webCrawlerTaskDispatcher;

    @BeforeEach
    void setUp() {
        underTest = new WebCrawlerTaskWorkerImpl(
                webCrawlerJobExecutionRepository,
                webCrawlerVisitedUrlRepository,
                urlVisitor,
                webCrawlerTaskDispatcher
        );
    }

    @Test
    void should_consume_a_web_crawler_task_typical() {
        // GIVEN
        UUID jobId = UUID.randomUUID();
        Integer currentLevel = 0;
        final URI currentUrl = UriComponentsBuilder.fromHttpUrl("http://example.com").build().toUri();
        WebCrawlerTask webCrawlerTask = WebCrawlerTask
                .builder()
                .webCrawlerJob(jobId)
                .level(currentLevel)
                .urlToVisit(currentUrl)
                .build();

        Long politenessDelay = 250L;
        WebCrawlerJobExecution webCrawlerJobExecution = WebCrawlerJobExecution
                .builder()
                .frontier(currentUrl)
                .id(jobId)
                .politenessDelayMillis(politenessDelay)
                .maxDepthOfCrawling(1)
                .build();
        given(webCrawlerJobExecutionRepository.findById(any())).willReturn(Optional.of(webCrawlerJobExecution));
        given(webCrawlerVisitedUrlRepository.findById(any())).willReturn(Optional.empty());

        final URI withinSubdomainUrl = UriComponentsBuilder.fromHttpUrl("http://example.com/foo/bar").build().toUri();
        final URI filteredOutUrl = UriComponentsBuilder.fromHttpUrl("http://example.org/i/will/be/discarded").build().toUri();

        given(urlVisitor.visitUrlAndGetHyperLinks(any())).willReturn(Set.of(
                withinSubdomainUrl,
                filteredOutUrl
        ));

        // WHEN
        underTest.consumeWebCrawlerTask(webCrawlerTask);

        // THEN
        ArgumentCaptor<WebCrawlerVisitedUrl> visitedUrlCaptor = ArgumentCaptor.forClass(WebCrawlerVisitedUrl.class);
        verify(webCrawlerVisitedUrlRepository, times(1)).save(visitedUrlCaptor.capture());
        assertThat(visitedUrlCaptor.getValue().getId()).isEqualTo(jobId.toString().concat(":").concat(currentUrl.toString()));
        assertThat(visitedUrlCaptor.getValue().getVisitedUrl()).isEqualTo(currentUrl);
        assertThat(visitedUrlCaptor.getValue().getLevel()).isEqualTo(currentLevel);
        assertThat(visitedUrlCaptor.getValue().getChildren()).containsExactlyInAnyOrder(withinSubdomainUrl);

        ArgumentCaptor<WebCrawlerTask> taskArgumentCaptor = ArgumentCaptor.forClass(WebCrawlerTask.class);
        verify(webCrawlerTaskDispatcher, times(1)).dispatch(taskArgumentCaptor.capture(), eq(politenessDelay));
        assertThat(taskArgumentCaptor.getValue().getUrlToVisit()).isEqualTo(withinSubdomainUrl);
        assertThat(taskArgumentCaptor.getValue().getWebCrawlerJob()).isEqualTo(jobId);
        assertThat(taskArgumentCaptor.getValue().getLevel()).isEqualTo(currentLevel + 1);
    }

    @Test
    void should_not_consume_a_web_crawler_task_because_got_past_depth() {
        // GIVEN
        UUID jobId = UUID.randomUUID();
        Integer currentLevel = 3;
        final URI currentUrl = UriComponentsBuilder.fromHttpUrl("http://example.com").build().toUri();
        WebCrawlerTask webCrawlerTask = WebCrawlerTask
                .builder()
                .webCrawlerJob(jobId)
                .level(currentLevel)
                .urlToVisit(currentUrl)
                .build();

        Long politenessDelay = 250L;
        WebCrawlerJobExecution webCrawlerJobExecution = WebCrawlerJobExecution
                .builder()
                .frontier(currentUrl)
                .id(jobId)
                .politenessDelayMillis(politenessDelay)
                .maxDepthOfCrawling(2)
                .build();
        given(webCrawlerJobExecutionRepository.findById(any())).willReturn(Optional.of(webCrawlerJobExecution));

        // WHEN
        underTest.consumeWebCrawlerTask(webCrawlerTask);

        // THEN
        verifyNoInteractions(webCrawlerVisitedUrlRepository);
        verifyNoInteractions(urlVisitor);
        verifyNoInteractions(webCrawlerTaskDispatcher);

    }

    @Test
    void should_not_consume_a_web_crawler_task_because_url_already_visited() {
        // GIVEN
        UUID jobId = UUID.randomUUID();
        Integer currentLevel = 0;
        final URI currentUrl = UriComponentsBuilder.fromHttpUrl("http://example.com").build().toUri();
        WebCrawlerTask webCrawlerTask = WebCrawlerTask
                .builder()
                .webCrawlerJob(jobId)
                .level(currentLevel)
                .urlToVisit(currentUrl)
                .build();

        Long politenessDelay = 250L;
        WebCrawlerJobExecution webCrawlerJobExecution = WebCrawlerJobExecution
                .builder()
                .frontier(currentUrl)
                .id(jobId)
                .politenessDelayMillis(politenessDelay)
                .maxDepthOfCrawling(1)
                .build();
        given(webCrawlerJobExecutionRepository.findById(any())).willReturn(Optional.of(webCrawlerJobExecution));
        given(webCrawlerVisitedUrlRepository.findById(any())).willReturn(Optional.of(WebCrawlerVisitedUrl.builder().build()));

        // WHEN
        underTest.consumeWebCrawlerTask(webCrawlerTask);

        // THEN
        verify(webCrawlerVisitedUrlRepository, times(1)).findById(eq(jobId.toString().concat(":").concat(currentUrl.toString())));
        verifyNoMoreInteractions(webCrawlerVisitedUrlRepository);
        verifyNoInteractions(urlVisitor);
        verifyNoInteractions(webCrawlerTaskDispatcher);
    }
}