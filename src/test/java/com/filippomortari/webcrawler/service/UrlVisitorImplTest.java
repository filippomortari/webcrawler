package com.filippomortari.webcrawler.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

public class UrlVisitorImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888).httpsPort(8889));

    private UrlVisitor urlVisitor;


    @Test
    public void should_fetch_urls_correctly() {
        // GIVEN
        urlVisitor = new UrlVisitorImpl();
        String html = "<html>\n"
                + "     <body>\n"
                + "         <p>Hello, World</p>\n"
                + "         <a href=\"http://www.example.com\">Example</a>\n"
                + "         <a href=\"http://www.foo.com?cookie=cream&some=other\">Example with query string</a>\n"
                + "         <a href=\"http://www.something.com/some_page/#fragment\">Example with fragment</a>\n"
                + "         <a href=\"http://www.something-else.com?var=var#fragment\">Example with query string and fragment</a>\n"
                + "         <p>Hello, World</p>\n"
                + "         <p>Hello, World</p>\n"
                + "     </body>\n"
                + "</html>";

        stubFor(
                get(urlEqualTo("/foo"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "text/html")
                                .withBody(html))

        );

        // WHEN
        final URI uri = UriComponentsBuilder.fromHttpUrl(
                format("http://localhost:%d/foo", wireMockRule.port())
        ).build().toUri();
        final Set<URI> results = urlVisitor.visitUrlAndGetHyperLinks(uri);

        // THEN
        assertThat(results).containsExactlyInAnyOrder(
                UriComponentsBuilder.fromHttpUrl("http://www.example.com").build().toUri(),
                UriComponentsBuilder.fromHttpUrl("http://www.foo.com").build().toUri(),
                UriComponentsBuilder.fromHttpUrl("http://www.something.com/some_page").build().toUri(),
                UriComponentsBuilder.fromHttpUrl("http://www.something-else.com").build().toUri()
        );
    }

    @Test
    public void should_handle_exceptions_gracefully() {
        // GIVEN
        urlVisitor = new UrlVisitorImpl();

        stubFor(
                get(urlEqualTo("/foo"))
                        .willReturn(aResponse()
                                .withStatus(500)
                                .withHeader("Content-Type", "text/html")
                                )

        );

        // WHEN
        final URI uri = UriComponentsBuilder.fromHttpUrl(
                format("http://localhost:%d/foo", wireMockRule.port())
        ).build().toUri();
        final Set<URI> results = urlVisitor.visitUrlAndGetHyperLinks(uri);

        // THEN
        assertThat(results).isEmpty();
    }
}