package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerTask;

public interface WebCrawlerTaskDispatcher {

    void dispatch(WebCrawlerTask webCrawlerTask, Long politenessDelayMillis);
}
