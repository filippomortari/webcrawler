package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerTask;
import com.github.sonus21.rqueue.core.RqueueMessageSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WebCrawlerTaskDispatcherImpl implements WebCrawlerTaskDispatcher {
    private final RqueueMessageSender rqueueMessageSender;
    private final String webCrawlerTasksQueue;

    public WebCrawlerTaskDispatcherImpl(
            RqueueMessageSender rqueueMessageSender,
            @Value("${webcrawler-tasks.queue.name}") final String webCrawlerTasksQueue
    ) {
        this.rqueueMessageSender = rqueueMessageSender;
        this.webCrawlerTasksQueue = webCrawlerTasksQueue;
    }

    @Override
    public void dispatch(WebCrawlerTask webCrawlerTask, Long politenessDelayMillis) {
        rqueueMessageSender.enqueueIn(webCrawlerTasksQueue, webCrawlerTask, politenessDelayMillis);
    }
}
