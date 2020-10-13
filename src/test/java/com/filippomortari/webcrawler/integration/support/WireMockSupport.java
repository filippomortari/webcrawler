package com.filippomortari.webcrawler.integration.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class WireMockSupport {
    private static WireMockServer wireMockServer;

    public static void bootstrapServer() {
        wireMockServer = new WireMockServer(
                options()
                        .dynamicPort()
                        .extensions(new DynamicTransformer())
        );
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());
    }

    public static void reset() {
        WireMock.reset();
    }

    public static void stopServer() {
        wireMockServer.stop();
    }

    public static Integer getServerPort() {
        return Optional
                .ofNullable(wireMockServer)
                .map(WireMockServer::port)
                .orElse(null);
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
            return "url-to-webpage-transformer";
        }
    }
}
