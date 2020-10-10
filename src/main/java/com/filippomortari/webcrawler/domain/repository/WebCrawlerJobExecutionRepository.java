package com.filippomortari.webcrawler.domain.repository;

import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WebCrawlerJobExecutionRepository extends CrudRepository<WebCrawlerJobExecution, UUID> {
}
