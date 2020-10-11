package com.filippomortari.webcrawler.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("webcrawlerjobs")
public class WebCrawlerJobExecution {

    @Id
    private UUID id;
    private Instant submittedOn;

    private URI frontier;

    private Integer maxDepthOfCrawling;
    private Long politenessDelayMillis;
}
