package com.filippomortari.webcrawler.service;

import com.filippomortari.webcrawler.domain.WebCrawlerTask;
import com.github.sonus21.rqueue.core.RqueueMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebCrawlerTaskDispatcherImplTest {

    private static final String QUEUE_NAME = "queue-name";
    private WebCrawlerTaskDispatcher underTest;
    @Mock
    private RqueueMessageSender rqueueMessageSender;

    @BeforeEach
    void setUp() {
        underTest = new WebCrawlerTaskDispatcherImpl(rqueueMessageSender, QUEUE_NAME);
    }

    @Test
    void should_dispatch_correctly() {
        // GIVEN
        WebCrawlerTask webCrawlerTask = WebCrawlerTask.builder().build();

        // WHEN
        underTest.dispatch(webCrawlerTask, 123L);

        // THEN
        verify(rqueueMessageSender, times(1))
                .enqueueIn(eq(QUEUE_NAME), eq(webCrawlerTask), eq(123L));
    }
}