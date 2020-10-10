package com.filippomortari.webcrawler.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebCrawlerTask {

    private URI urlToVisit;
    private UUID webCrawlerJob;
    private Integer level;

}
