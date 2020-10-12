package com.filippomortari.webcrawler.controller;

import com.filippomortari.webcrawler.controller.dto.WebCrawlerVisitedUrlDTO;
import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;
import com.filippomortari.webcrawler.service.WebCrawlerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

    @ApiOperation(value = "Submits a new web crawler job")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful job creation, returns a Location header"),
            @ApiResponse(code = 400, message = "Request payload validation failure"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
    })
    @PostMapping(path = "jobs", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> submitNewJob(@Valid @RequestBody WebCrawlerJobRequest webCrawlerJobRequest) {
        WebCrawlerJobExecution webCrawlerJobExecution = webCrawlerService.submitNewJob(webCrawlerJobRequest);

        final URI jobExecutionLocation = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .pathSegment("crawler")
                .pathSegment("jobs")
                .pathSegment("{jobExecutionId}")
                .build(webCrawlerJobExecution.getId());

        return ResponseEntity.created(jobExecutionLocation).build();
    }

    @ApiOperation(value = "Describes a web crawler job definition")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a web crawler job definition"),
            @ApiResponse(code = 400, message = "Missing job ID as a path param"),
            @ApiResponse(code = 404, message = "Job definition not found"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
    })
    @GetMapping(path = "jobs/{jobExecutionId}", produces = "application/json")
    public ResponseEntity<WebCrawlerJobExecution> getJobDefinition(@PathVariable @Valid UUID jobExecutionId) {
        Optional<WebCrawlerJobExecution> webCrawlerJobExecution = webCrawlerService.getJobExecution(jobExecutionId);
        return ResponseEntity.of(webCrawlerJobExecution);
    }

    @ApiOperation(value = "Describes a web crawler job activity")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a web crawler job activity"),
            @ApiResponse(code = 400, message = "Missing job ID as a path param"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
    })
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
