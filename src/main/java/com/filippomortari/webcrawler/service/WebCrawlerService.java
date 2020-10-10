package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;

import java.util.Optional;
import java.util.UUID;

public interface WebCrawlerService {

    WebCrawlerJobExecution submitNewJob(WebCrawlerJobRequest webCrawlerJobRequest);

    Optional<WebCrawlerJobExecution> getJobExecution(UUID webCrawlerJobExecutionId);
}
