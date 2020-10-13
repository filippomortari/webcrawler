package com.filippomortari.webcrawler.integration;

import com.filippomortari.webcrawler.controller.dto.WebCrawlerVisitedUrlDTO;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;
import com.filippomortari.webcrawler.integration.support.AbstractIT;
import com.filippomortari.webcrawler.integration.support.WireMockSupport;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;


public class WebCrawlerIT extends AbstractIT {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void startsApplicationContext() {
    }

    @Test
    void web_crawler_e2e() {
        // GIVEN
        stubFor(get(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withTransformers("url-to-webpage-transformer")
                )
        );

        // WHEN
        final URI location = restTemplate.postForLocation(
                format("http://localhost:%d/crawler/jobs", serverPort),
                WebCrawlerJobRequest
                        .builder()
                        .frontier(format("http://localhost:%d/foo", WireMockSupport.getServerPort()))
                        .maxDepthOfCrawling(2)
                        .build()
        );

        // THEN
        Awaitility
                .await()
                .pollDelay(1L, TimeUnit.SECONDS)
                .atMost(5L, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    final ResponseEntity<List<WebCrawlerVisitedUrlDTO>> exchange = restTemplate.exchange(
                            format("%s/activity", location),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<WebCrawlerVisitedUrlDTO>>() {
                            });

                    assertThat(exchange.getBody().size()).isEqualTo(7);
                });

    }

}


