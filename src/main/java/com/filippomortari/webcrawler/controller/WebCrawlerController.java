package com.filippomortari.webcrawler.controller;

import com.filippomortari.webcrawler.controller.dto.WebCrawlerVisitedUrlDTO;
import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;
import com.filippomortari.webcrawler.service.WebCrawlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("crawler")
public class WebCrawlerController {

    private final WebCrawlerService webCrawlerService;

    public WebCrawlerController(WebCrawlerService webCrawlerService) {
        this.webCrawlerService = webCrawlerService;
    }

    @PostMapping(path = "jobs", consumes = "application/json", produces = "application/json")
    public ResponseEntity<WebCrawlerJobExecution> submitNewJob(@Valid @RequestBody WebCrawlerJobRequest webCrawlerJobRequest) {
        WebCrawlerJobExecution webCrawlerJobExecution = webCrawlerService.submitNewJob(webCrawlerJobRequest);

        final URI jobExecutionLocation = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .pathSegment("crawler")
                .pathSegment("jobs")
                .pathSegment("{jobExecutionId}")
                .build(webCrawlerJobExecution.getId());

        return ResponseEntity.created(jobExecutionLocation).build();
    }

    @GetMapping(path = "jobs/{jobExecutionId}", produces = "application/json")
    public ResponseEntity<WebCrawlerJobExecution> getJobDefinition(@PathVariable @Valid UUID jobExecutionId) {
        Optional<WebCrawlerJobExecution> webCrawlerJobExecution = webCrawlerService.getJobExecution(jobExecutionId);
        return ResponseEntity.of(webCrawlerJobExecution);
    }

    @GetMapping(path = "jobs/{jobExecutionId}/activity", produces = "application/json")
    public ResponseEntity<List<WebCrawlerVisitedUrlDTO>> listActivity(@PathVariable @Valid UUID jobExecutionId) {
        final List<WebCrawlerVisitedUrlDTO> results = webCrawlerService
                .listActivity(jobExecutionId)
                .parallelStream()
                .map(WebCrawlerVisitedUrlDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

}
