package com.filippomortari.webcrawler.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class UrlVisitorImpl implements UrlVisitor {
    @Override
    public Set<URI> visitUrlAndGetHyperLinks(URI uri) {
        Document document;
        try {
            document = Jsoup.connect(uri.toString())
                    .timeout(5000)
                    .get();
        } catch (IOException e) {
            log.warn("Unable to fetch {} due to: {}", uri, e.toString());
            return new HashSet<>();
        }

        return document
                .select("a")
                .stream()
                .map(e -> e.attr("abs:href"))
                .map(this::trimQueryString)
                .map(url -> UriComponentsBuilder.fromHttpUrl(url).build().toUri())
                .collect(Collectors.toSet());
    }

    public String trimQueryString(String url) {
            if (url == null) return null;
            String r = url.split("\\?")[0].split("#")[0];
            if (r.endsWith("/")) {
                return r.substring(0, r.length() - 1);
            }
            return r;
    }
}
