package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;
import com.filippomortari.webcrawler.domain.WebCrawlerTask;
import com.filippomortari.webcrawler.domain.WebCrawlerVisitedUrl;
import com.filippomortari.webcrawler.domain.repository.WebCrawlerJobExecutionRepository;
import com.filippomortari.webcrawler.domain.repository.WebCrawlerVisitedUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WebCrawlerServiceImpl implements WebCrawlerService {

    private final WebCrawlerJobExecutionRepository webCrawlerJobExecutionRepository;
    private final WebCrawlerTaskDispatcher webCrawlerTaskDispatcher;
    private final WebCrawlerVisitedUrlRepository webCrawlerVisitedUrlRepository;


    public WebCrawlerServiceImpl(
            final WebCrawlerJobExecutionRepository webCrawlerJobExecutionRepository,
            final WebCrawlerTaskDispatcher webCrawlerTaskDispatcher,
            WebCrawlerVisitedUrlRepository webCrawlerVisitedUrlRepository) {
        this.webCrawlerJobExecutionRepository = webCrawlerJobExecutionRepository;
        this.webCrawlerTaskDispatcher = webCrawlerTaskDispatcher;
        this.webCrawlerVisitedUrlRepository = webCrawlerVisitedUrlRepository;
    }

    @Override
    public WebCrawlerJobExecution submitNewJob(WebCrawlerJobRequest webCrawlerJobRequest) {
        WebCrawlerJobExecution webCrawlerJobExecution = WebCrawlerJobExecution
                .builder()
                .id(UUID.randomUUID())
                .submittedOn(Instant.now())
                .frontier(UriComponentsBuilder.fromHttpUrl(webCrawlerJobRequest.getFrontier()).build().toUri())
                .maxDepthOfCrawling(webCrawlerJobRequest.getMaxDepthOfCrawling())
                .politenessDelayMillis(webCrawlerJobRequest.getPolitenessDelayMillis())
                .build();

        final WebCrawlerJobExecution saved = webCrawlerJobExecutionRepository.save(webCrawlerJobExecution);
        final WebCrawlerTask webCrawlerTask = WebCrawlerTask
                .builder()
                .urlToVisit(saved.getFrontier())
                .webCrawlerJob(saved.getId())
                .level(0)
                .build();

        webCrawlerTaskDispatcher.dispatch(webCrawlerTask, 0L);

        return saved;
    }

    @Override
    public Optional<WebCrawlerJobExecution> getJobExecution(UUID webCrawlerJobExecutionId) {
        return webCrawlerJobExecutionRepository.findById(webCrawlerJobExecutionId);
    }

    @Override
    public List<WebCrawlerVisitedUrl> listActivity(UUID webCrawlerJobExecutionId) {
        return webCrawlerVisitedUrlRepository.findByWebCrawlerJobId(webCrawlerJobExecutionId);
    }
}
