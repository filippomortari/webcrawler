package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;
import com.filippomortari.webcrawler.domain.WebCrawlerVisitedUrl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebCrawlerService {

    WebCrawlerJobExecution submitNewJob(WebCrawlerJobRequest webCrawlerJobRequest);

    Optional<WebCrawlerJobExecution> getJobExecution(UUID webCrawlerJobExecutionId);

    List<WebCrawlerVisitedUrl> listActivity(UUID webCrawlerJobExecutionId);
}
