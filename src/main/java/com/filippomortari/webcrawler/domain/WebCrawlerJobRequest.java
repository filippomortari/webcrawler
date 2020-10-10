package com.filippomortari.webcrawler.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebCrawlerJobRequest {

    @URL
    private String frontier;

    private Integer maxDepthOfCrawling;
    private Integer maxPagesToFetch;
    private Long politenessDelayMillis;
}
