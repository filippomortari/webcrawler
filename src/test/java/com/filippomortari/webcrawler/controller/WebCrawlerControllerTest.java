package com.filippomortari.webcrawler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filippomortari.webcrawler.domain.WebCrawlerJobExecution;
import com.filippomortari.webcrawler.domain.WebCrawlerJobRequest;
import com.filippomortari.webcrawler.domain.WebCrawlerVisitedUrl;
import com.filippomortari.webcrawler.service.WebCrawlerService;
import com.github.sonus21.rqueue.core.RqueueMessageSender;
import com.github.sonus21.rqueue.spring.boot.RqueueListenerAutoConfig;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {RedisRepositoriesAutoConfiguration.class, RqueueListenerAutoConfig.class})
class WebCrawlerControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @MockBean
    private WebCrawlerService webCrawlerService;
    @MockBean
    private RqueueMessageSender rqueueMessageSender;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_accept_a_new_webcrawler_job() throws Exception {

        // GIVEN
        WebCrawlerJobExecution webCrawlerJobExecution = mock(WebCrawlerJobExecution.class);
        given(webCrawlerService.submitNewJob(any())).willReturn(webCrawlerJobExecution);
        UUID jobId = UUID.randomUUID();
        given(webCrawlerJobExecution.getId()).willReturn(jobId);

        final String request = new JSONObject()
                .put("frontier", "https://monzo.com")
                .put("politenessDelayMillis", 500)
                .put("maxDepthOfCrawling", 2)
                .toString();

        // WHEN, THEN
        mockMvc.perform(
                post("/crawler/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        containsString(format("/crawler/jobs/%s", jobId))
                        )
                );

        final WebCrawlerJobRequest webCrawlerJobRequest = OBJECT_MAPPER.readValue(request, WebCrawlerJobRequest.class);
        verify(webCrawlerService, times(1)).submitNewJob(eq(webCrawlerJobRequest));
    }

    @Test
    void should_reject_malformed_urls() throws Exception {

        // GIVEN
        WebCrawlerJobExecution webCrawlerJobExecution = mock(WebCrawlerJobExecution.class);
        given(webCrawlerService.submitNewJob(any())).willReturn(webCrawlerJobExecution);
        UUID jobId = UUID.randomUUID();
        given(webCrawlerJobExecution.getId()).willReturn(jobId);

        final String request = new JSONObject()
                .put("frontier", "foo")
                .toString();

        // WHEN, THEN
        mockMvc.perform(
                post("/crawler/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(webCrawlerService);
    }

    @Test
    void should_return_a_webcrawler_job_execution() throws Exception {
        // GIVEN
        UUID jobId = UUID.randomUUID();
        WebCrawlerJobExecution webCrawlerJobExecution = WebCrawlerJobExecution
                .builder()
                .frontier(UriComponentsBuilder.fromHttpUrl("https://example.com").build().toUri())
                .submittedOn(Instant.now())
                .id(jobId)
                .build();
        given(webCrawlerService.getJobExecution(any())).willReturn(Optional.of(webCrawlerJobExecution));

        // WHEN, THEN
        mockMvc.perform(
                get(format("/crawler/jobs/%s", jobId))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frontier").value("https://example.com"))
                .andExpect(jsonPath("$.id").value(jobId.toString()));
    }

    @Test
    void should_return_not_found_for_a_missing_job_execution() throws Exception {
        // GIVEN
        UUID jobId = UUID.randomUUID();
        given(webCrawlerService.getJobExecution(any())).willReturn(Optional.empty());

        // WHEN, THEN
        mockMvc.perform(
                get(format("/crawler/jobs/%s", jobId))
        )
                .andExpect(status().isNotFound());

        verify(webCrawlerService, times(1)).getJobExecution(eq(jobId));
    }

    @Test
    void should_return_a_list_of_crawler_activities_for_a_given_job_execution() throws Exception {
        // GIVEN
        UUID jobId = UUID.randomUUID();
        given(webCrawlerService.listActivity(any())).willReturn(List.of(
                WebCrawlerVisitedUrl.builder().level(1).webCrawlerJobId(jobId).visitedUrl(
                        UriComponentsBuilder.fromHttpUrl("https://example.com").build().toUri()
                ).build(),
                WebCrawlerVisitedUrl.builder().level(1).webCrawlerJobId(jobId).visitedUrl(
                        UriComponentsBuilder.fromHttpUrl("https://google.com").build().toUri()
                ).build()
        ));

        // WHEN, THEN
        mockMvc.perform(
                get(format("/crawler/jobs/%s/activity", jobId))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].visitedUrl").value("https://example.com"))
                .andExpect(jsonPath("$[1].visitedUrl").value("https://google.com"))

        ;

        verify(webCrawlerService, times(1)).listActivity(eq(jobId));

    }
}