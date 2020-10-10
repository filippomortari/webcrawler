package com.filippomortari.webcrawler.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebCrawlerJobExecution {

    private UUID id;
    private Instant submittedOn;

    private URI frontier;

    private Integer maxDepthOfCrawling;
    private Integer maxPagesToFetch;
    private Integer politenessDelayMillis;
}
