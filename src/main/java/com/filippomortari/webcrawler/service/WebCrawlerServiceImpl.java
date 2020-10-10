package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.UUID;

@Service
public class WebCrawlerServiceImpl implements WebCrawlerService {
    @Override
    public WebCrawlerJobExecution submitNewJob(WebCrawlerJobRequest webCrawlerJobRequest) {
        System.out.println(webCrawlerJobRequest.toString());
        WebCrawlerJobExecution webCrawlerJobExecution = WebCrawlerJobExecution
                .builder()
                .id(UUID.randomUUID())
                .submittedOn(Instant.now())
                .frontier(UriComponentsBuilder.fromHttpUrl(webCrawlerJobRequest.getFrontier()).build().toUri())
                .maxDepthOfCrawling(webCrawlerJobRequest.getMaxDepthOfCrawling())
                .maxPagesToFetch(webCrawlerJobRequest.getMaxPagesToFetch())
                .politenessDelayMillis(webCrawlerJobRequest.getPolitenessDelayMillis())
                .build();

        return webCrawlerJobExecution;
    }
}
