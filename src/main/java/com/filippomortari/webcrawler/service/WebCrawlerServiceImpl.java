package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;
import com.filippomortari.webcrawler.domain.WebCrawlerTask;
import com.filippomortari.webcrawler.domain.repository.WebCrawlerJobExecutionRepository;
import com.github.sonus21.rqueue.core.RqueueMessageSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class WebCrawlerServiceImpl implements WebCrawlerService {

    private final WebCrawlerJobExecutionRepository webCrawlerJobExecutionRepository;
    private final WebCrawlerTaskDispatcher webCrawlerTaskDispatcher;


    public WebCrawlerServiceImpl(
            final WebCrawlerJobExecutionRepository webCrawlerJobExecutionRepository,
            final WebCrawlerTaskDispatcher webCrawlerTaskDispatcher
    ) {
        this.webCrawlerJobExecutionRepository = webCrawlerJobExecutionRepository;
        this.webCrawlerTaskDispatcher = webCrawlerTaskDispatcher;
    }

    @Override
    public WebCrawlerJobExecution submitNewJob(WebCrawlerJobRequest webCrawlerJobRequest) {
        WebCrawlerJobExecution webCrawlerJobExecution = WebCrawlerJobExecution
                .builder()
                .id(UUID.randomUUID())
                .submittedOn(Instant.now())
                .frontier(UriComponentsBuilder.fromHttpUrl(webCrawlerJobRequest.getFrontier()).build().toUri())
                .maxDepthOfCrawling(webCrawlerJobRequest.getMaxDepthOfCrawling())
                .maxPagesToFetch(webCrawlerJobRequest.getMaxPagesToFetch())
                .politenessDelayMillis(webCrawlerJobRequest.getPolitenessDelayMillis())
                .build();

        final WebCrawlerJobExecution saved = webCrawlerJobExecutionRepository.save(webCrawlerJobExecution);
        final WebCrawlerTask webCrawlerTask = WebCrawlerTask
                .builder()
                .urlToVisit(saved.getFrontier())
                .webCrawlerJob(saved.getId())
                .level(0)
                .build();

        webCrawlerTaskDispatcher.dispatch(webCrawlerTask, saved.getPolitenessDelayMillis());

        return saved;
    }

    @Override
    public Optional<WebCrawlerJobExecution> getJobExecution(UUID webCrawlerJobExecutionId) {
        return webCrawlerJobExecutionRepository.findById(webCrawlerJobExecutionId);
    }
}
