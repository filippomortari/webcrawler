package com.filippomortari.webcrawler.controller;

import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;
import com.filippomortari.webcrawler.service.WebCrawlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("crawler")
public class WebCrawlerController {

    private final WebCrawlerService webCrawlerService;

    public WebCrawlerController(WebCrawlerService webCrawlerService) {
        this.webCrawlerService = webCrawlerService;
    }

    @PostMapping(path = "jobs")
    public ResponseEntity<WebCrawlerJobExecution> submitNewJob(@Valid WebCrawlerJobRequest webCrawlerJobRequest) {
        WebCrawlerJobExecution webCrawlerJobExecution = webCrawlerService.submitNewJob(webCrawlerJobRequest);

        URI jobExecutionLocation = UriComponentsBuilder
                .fromPath("jobs")
                .pathSegment("{jobExecutionId}")
                .build(webCrawlerJobExecution.getId());

        return ResponseEntity.created(jobExecutionLocation).build();

    }

}
