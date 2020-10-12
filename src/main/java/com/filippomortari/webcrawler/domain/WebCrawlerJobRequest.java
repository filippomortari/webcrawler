package com.filippomortari.webcrawler.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebCrawlerJobRequest {

    @URL
    @NotEmpty
    private String frontier;

    private Integer maxDepthOfCrawling;
    private Long politenessDelayMillis;
}
