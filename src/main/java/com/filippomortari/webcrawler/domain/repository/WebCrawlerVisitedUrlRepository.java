package com.filippomortari.webcrawler.domain.repository;

import com.filippomortari.webcrawler.domain.WebCrawlerVisitedUrl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebCrawlerVisitedUrlRepository extends CrudRepository<WebCrawlerVisitedUrl, String> {

    List<WebCrawlerVisitedUrl> findByWebCrawlerJobId(UUID webCrawlerJobId);
}
