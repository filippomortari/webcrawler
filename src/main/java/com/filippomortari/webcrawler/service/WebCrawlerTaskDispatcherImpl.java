package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerTask;
import com.github.sonus21.rqueue.core.RqueueMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebCrawlerTaskDispatcherImpl implements WebCrawlerTaskDispatcher {
    private final RqueueMessageSender rqueueMessageSender;
    private final String webCrawlerTasksQueue;

    public WebCrawlerTaskDispatcherImpl(
            final RqueueMessageSender rqueueMessageSender,
            @Value("${webcrawler-tasks.queue.name}") final String webCrawlerTasksQueue
    ) {
        this.rqueueMessageSender = rqueueMessageSender;
        this.webCrawlerTasksQueue = webCrawlerTasksQueue;
    }

    @Override
    public void dispatch(WebCrawlerTask webCrawlerTask, Long politenessDelayMillis) {
        log.debug("Dispatching task {}", webCrawlerTask);
        rqueueMessageSender.enqueueIn(webCrawlerTasksQueue, webCrawlerTask, politenessDelayMillis);
    }
}
