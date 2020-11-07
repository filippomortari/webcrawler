package com.filippomortari.webcrawler.integration.support;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIT {
    @BeforeAll
    static void setUp() {
        WireMockSupport.bootstrapServer();
    }

    @AfterEach
    void tearDownEach() {
        WireMockSupport.reset();
    }

    @AfterAll
    static void tearDown() {
        WireMockSupport.stopServer();
    }

    @Container
    static GenericContainer redisContainer = new GenericContainer("redis:6.0.9").withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("spring.redis.host", redisContainer::getContainerIpAddress);
        r.add("spring.redis.port", redisContainer::getFirstMappedPort);
    }
}
