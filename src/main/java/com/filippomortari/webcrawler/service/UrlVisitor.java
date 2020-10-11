package com.filippomortari.webcrawler.service;

import java.net.URI;
import java.util.Set;

public interface UrlVisitor {
    Set<URI> visitUrlAndGetHyperLinks(URI uri);
}
