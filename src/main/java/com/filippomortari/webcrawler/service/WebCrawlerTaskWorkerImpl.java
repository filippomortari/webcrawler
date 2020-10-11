package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerTask;
import com.filippomortari.webcrawler.domain.WebCrawlerVisitedUrl;
import com.filippomortari.webcrawler.domain.repository.WebCrawlerJobExecutionRepository;
import com.filippomortari.webcrawler.domain.repository.WebCrawlerVisitedUrlRepository;
import com.github.sonus21.rqueue.annotation.RqueueListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WebCrawlerTaskWorkerImpl implements WebCrawlerTaskWorker {

    private final WebCrawlerJobExecutionRepository webCrawlerJobExecutionRepository;
    private final WebCrawlerVisitedUrlRepository webCrawlerVisitedUrlRepository;
    private final UrlVisitor urlVisitor;
    private final WebCrawlerTaskDispatcher webCrawlerTaskDispatcher;

    public WebCrawlerTaskWorkerImpl(
            WebCrawlerJobExecutionRepository webCrawlerJobExecutionRepository,
            WebCrawlerVisitedUrlRepository webCrawlerVisitedUrlRepository,
            UrlVisitor urlVisitor,
            WebCrawlerTaskDispatcher webCrawlerTaskDispatcher
    ) {
        this.webCrawlerJobExecutionRepository = webCrawlerJobExecutionRepository;
        this.webCrawlerVisitedUrlRepository = webCrawlerVisitedUrlRepository;
        this.urlVisitor = urlVisitor;
        this.webCrawlerTaskDispatcher = webCrawlerTaskDispatcher;
    }


    @RqueueListener(value = "${email.queue.name}")
    public void consumeWebCrawlerTask(WebCrawlerTask webCrawlerTask) {
        log.debug("Received task {}", webCrawlerTask);

        final UUID webCrawlerJobId = webCrawlerTask.getWebCrawlerJob();

        final Optional<WebCrawlerJobExecution> webCrawlerJobExecutionOptional = webCrawlerJobExecutionRepository.findById(webCrawlerJobId);

        if (webCrawlerJobExecutionOptional.isPresent()) {
            final URI urlToVisit = webCrawlerTask.getUrlToVisit();
            final Set<URI> urisFound = urlVisitor.visitUrlAndGetHyperLinks(urlToVisit);

            final WebCrawlerJobExecution webCrawlerJobExecution = webCrawlerJobExecutionOptional.get();

            final Set<URI> urisFoundFilteredByFrontierDomain = urisFound
                    .stream()
                    .filter(url -> url.toString().startsWith(webCrawlerJobExecution.getFrontier().toString())).collect(Collectors.toSet());

            final Integer currentLevel = webCrawlerTask.getLevel();

            final WebCrawlerVisitedUrl webCrawlerVisitedUrl = WebCrawlerVisitedUrl
                    .builder()
                    .visitedUrl(urlToVisit)
                    .webCrawlerJobId(webCrawlerJobId)
                    .children(urisFoundFilteredByFrontierDomain) // TODO this? or the original set found? question for POs
                    .id(buildWebCrawlerVisitedUrlId(webCrawlerJobId, urlToVisit))
                    .visitedAt(Instant.now())
                    .level(currentLevel)
                    .build();

            log.debug("Visited: {}", webCrawlerVisitedUrl);
            webCrawlerVisitedUrlRepository.save(webCrawlerVisitedUrl);

            urisFoundFilteredByFrontierDomain
                    .stream()
                    .filter(uri -> isUriEligibleForCrawling(uri, webCrawlerTask.getLevel(), webCrawlerJobExecution))
                    .forEach(uri -> {
                        final WebCrawlerTask newTask = WebCrawlerTask
                                .builder()
                                .urlToVisit(uri)
                                .webCrawlerJob(webCrawlerJobId)
                                .level(currentLevel + 1)
                                .build();

                        webCrawlerTaskDispatcher.dispatch(newTask, webCrawlerJobExecution.getPolitenessDelayMillis());
                    });


        } else {
            // consume the message and error
            log.error("Unable to retrieve webcrawler job definition with id {}", webCrawlerJobId);
        }

    }

    private boolean isUriEligibleForCrawling(URI uri, Integer currentLevel, WebCrawlerJobExecution webCrawlerJobExecution) {
        final Integer maxDepthOfCrawling = webCrawlerJobExecution.getMaxDepthOfCrawling();
        if (Objects.nonNull(maxDepthOfCrawling)) {
            if (currentLevel >= maxDepthOfCrawling) {
                return false;
            }
        }
        final String potentiallyVisitedUrlId = this.buildWebCrawlerVisitedUrlId(webCrawlerJobExecution.getId(), uri);
        final Optional<WebCrawlerVisitedUrl> potentiallyVisited = webCrawlerVisitedUrlRepository.findById(potentiallyVisitedUrlId);

        return potentiallyVisited.isEmpty();
    }

    private String buildWebCrawlerVisitedUrlId(UUID webCrawlerJobId, URI visitedUrl) {
        return webCrawlerJobId.toString().concat(":").concat(visitedUrl.toString());
    }
}
