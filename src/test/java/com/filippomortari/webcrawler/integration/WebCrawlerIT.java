package com.filippomortari.webcrawler.integration;

import com.filippomortari.webcrawler.controller.dto.WebCrawlerVisitedUrlDTO;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;


public class WebCrawlerIT extends AbstractIT {
    private WireMockServer wireMockServer;

    @LocalServerPort
    private int serverPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(
                options()
                        .dynamicPort()
                .extensions(new DynamicTransformer())
        );
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void tearDown() {

        WireMock.reset();
        wireMockServer.stop();
    }

    @Test
    void startsApplicationContext() {
    }

    @Test
    void web_crawler_e2e() {
        // GIVEN
        stubFor(get(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withTransformers("dynamic-transformer")
                )
        );

        // WHEN
        final URI location = restTemplate.postForLocation(
                format("http://localhost:%d/crawler/jobs", serverPort),
                WebCrawlerJobRequest
                        .builder()
                        .frontier(format("http://localhost:%d/foo", wireMockServer.port()))
                        .maxDepthOfCrawling(2)
                        .politenessDelayMillis(150L).build()
        );

        Awaitility
                .await()
                .atLeast(5L, TimeUnit.SECONDS)
                .atMost(10L, TimeUnit.SECONDS);
//                .untilAsserted(() -> {
//                    final ResponseEntity<List<WebCrawlerVisitedUrlDTO>> exchange = restTemplate.exchange(
//                            format("http://localhost:%d/crawler/jobs/%s/activity", serverPort, location),
//                            HttpMethod.GET,
//                            null,
//                            new ParameterizedTypeReference<List<WebCrawlerVisitedUrlDTO>>() {
//                            });
//
//                    assertThat(exchange.getBody().size()).isEqualTo(7);
//                });

    }

    public static class DynamicTransformer extends ResponseDefinitionTransformer {
        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
            String path = request.getUrl().replaceAll("/", "|");
            String html;
            try {
                html = IOUtils.toString(
                        this.getClass().getResourceAsStream("/" + path),
                        "UTF-8"
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return new ResponseDefinitionBuilder()
                    .withHeader("Content-Type", "text/html")
                    .withStatus(200)
                    .withBody(html)
                    .build();
        }

        @Override
        public String getName() {
            return "dynamic-transformer";
        }
    }

}


