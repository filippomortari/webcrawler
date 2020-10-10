package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;

public interface WebCrawlerService {

    WebCrawlerJobExecution submitNewJob(WebCrawlerJobRequest webCrawlerJobRequest);

}
