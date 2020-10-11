package com.filippomortari.webcrawler.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("webcrawlervisitedurls")
public class WebCrawlerVisitedUrl {
    @Id
    private String id;
    @Indexed
    private UUID webCrawlerJobId;
    private URI visitedUrl;
    private Integer level;
    private Instant visitedAt;
    private Set<URI> children;
}
