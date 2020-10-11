package com.filippomortari.webcrawler.controller.dto;

import com.filippomortari.webcrawler.domain.WebCrawlerVisitedUrl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebCrawlerVisitedUrlDTO {
    private URI visitedUrl;
    private Integer level;
    private Instant visitedAt;
    private Set<URI> children;

    public WebCrawlerVisitedUrlDTO(WebCrawlerVisitedUrl webCrawlerVisitedUrl) {
        this.visitedUrl = webCrawlerVisitedUrl.getVisitedUrl();
        this.children = webCrawlerVisitedUrl.getChildren();
        this.level = webCrawlerVisitedUrl.getLevel();
        this.visitedAt = webCrawlerVisitedUrl.getVisitedAt();
    }
}
