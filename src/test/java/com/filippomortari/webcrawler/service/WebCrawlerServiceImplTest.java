package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;
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

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebCrawlerServiceImplTest {

    private WebCrawlerService underTest;
    @Mock
    private WebCrawlerJobExecutionRepository webCrawlerJobExecutionRepository;
    @Mock
    private WebCrawlerTaskDispatcher webCrawlerTaskDispatcher;
    @Mock
    private WebCrawlerVisitedUrlRepository webCrawlerVisitedUrlRepository;

    @BeforeEach
    void setUp() {
        underTest = new WebCrawlerServiceImpl(
                webCrawlerJobExecutionRepository,
                webCrawlerTaskDispatcher,
                webCrawlerVisitedUrlRepository
                );
    }

    @Test
    void should_submit_a_new_job() throws Exception {
        // GIVEN
        WebCrawlerJobRequest webCrawlerJobRequest = WebCrawlerJobRequest
                .builder()
                .frontier("http://example.com")
                .maxDepthOfCrawling(10)
                .politenessDelayMillis(250L)
                .build();
        final UUID jobId = UUID.randomUUID();
        WebCrawlerJobExecution webCrawlerJobExecution = mock(WebCrawlerJobExecution.class);
        given(webCrawlerJobExecutionRepository.save(any())).willReturn(webCrawlerJobExecution);
        given(webCrawlerJobExecution.getId()).willReturn(jobId);
        given(webCrawlerJobExecution.getFrontier()).willReturn(new URI("http://example.com"));
        // WHEN
        final WebCrawlerJobExecution result = underTest.submitNewJob(webCrawlerJobRequest);
        assertThat(result).isEqualTo(webCrawlerJobExecution);

        // THEN
        ArgumentCaptor<WebCrawlerJobExecution> jobExecutionArgumentCaptor = ArgumentCaptor.forClass(WebCrawlerJobExecution.class);
        verify(webCrawlerJobExecutionRepository, times(1)).save(jobExecutionArgumentCaptor.capture());
        assertThat(jobExecutionArgumentCaptor.getValue().getFrontier().toString()).isEqualTo("http://example.com");
        assertThat(jobExecutionArgumentCaptor.getValue().getMaxDepthOfCrawling()).isEqualTo(10L);
        assertThat(jobExecutionArgumentCaptor.getValue().getPolitenessDelayMillis()).isEqualTo(250L);
        assertThat(jobExecutionArgumentCaptor.getValue().getId()).isNotNull();
        assertThat(jobExecutionArgumentCaptor.getValue().getSubmittedOn()).isNotNull();

        ArgumentCaptor<WebCrawlerTask> webCrawlerTaskArgumentCaptor = ArgumentCaptor.forClass(WebCrawlerTask.class);
        verify(webCrawlerTaskDispatcher, times(1)).dispatch(webCrawlerTaskArgumentCaptor.capture(), eq(0L));

        assertThat(webCrawlerTaskArgumentCaptor.getValue().getUrlToVisit().toString()).isEqualTo("http://example.com");
        assertThat(webCrawlerTaskArgumentCaptor.getValue().getLevel()).isEqualTo(0);
        assertThat(webCrawlerTaskArgumentCaptor.getValue().getWebCrawlerJob()).isEqualTo(jobId);
    }

    @Test
    void should_find_a_job_execution_by_id() throws Exception {
        // GIVEN
        final UUID jobId = UUID.randomUUID();
        WebCrawlerJobExecution webCrawlerJobExecution = mock(WebCrawlerJobExecution.class);
        given(webCrawlerJobExecutionRepository.findById(any())).willReturn(Optional.of(webCrawlerJobExecution));

        // WHEN
        final Optional<WebCrawlerJobExecution> result = underTest.getJobExecution(jobId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(webCrawlerJobExecution);

        verify(webCrawlerJobExecutionRepository, times(1)).findById(eq(jobId));
    }

    @Test
    void should_retrieve_a_list_of_visited_urls() throws Exception {
        // GIVEN
        final UUID jobId = UUID.randomUUID();
        WebCrawlerVisitedUrl visitedUrl1 = mock(WebCrawlerVisitedUrl.class);
        WebCrawlerVisitedUrl visitedUrl2 = mock(WebCrawlerVisitedUrl.class);
        given(webCrawlerVisitedUrlRepository.findByWebCrawlerJobId(any())).willReturn(List.of(visitedUrl1, visitedUrl2));

        // WHEN
        final List<WebCrawlerVisitedUrl> result = underTest.listActivity(jobId);

        // THEN
        assertThat(result).containsExactlyInAnyOrder(visitedUrl1, visitedUrl2);

        verify(webCrawlerVisitedUrlRepository, times(1)).findByWebCrawlerJobId(eq(jobId));
    }
}