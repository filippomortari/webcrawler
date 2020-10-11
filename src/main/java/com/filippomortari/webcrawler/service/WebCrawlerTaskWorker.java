package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerTask;

public interface WebCrawlerTaskWorker {

    void consumeWebCrawlerTask(WebCrawlerTask webCrawlerTask);
}
