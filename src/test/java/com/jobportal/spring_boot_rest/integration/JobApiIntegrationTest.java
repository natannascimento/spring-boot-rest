package com.jobportal.spring_boot_rest.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class JobApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("jobs_db")
            .withUsername("jobs")
            .withPassword("jobs");

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("app.seed.enabled", () -> true);
        registry.add("app.seed.token", () -> "seed-secret");
    }

    @Test
    void loadEndpoint_ShouldBeIdempotentUnderConcurrentCalls() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<Integer>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> mockMvc.perform(post("/load").header("X-Seed-Token", "seed-secret"))
                    .andReturn()
                    .getResponse()
                    .getStatus());
        }

        List<Future<Integer>> results = executor.invokeAll(tasks);
        executor.shutdown();

        long createdResponses = results.stream().filter(this::isCreated).count();
        long okResponses = results.stream().filter(this::isOk).count();

        assertEquals(1, createdResponses, "Exactly one request should load seed data");
        assertEquals(threadCount - 1, okResponses, "Remaining requests should be idempotent");
    }

    @Test
    void loadEndpoint_ShouldReturnForbidden_WhenTokenInvalid() throws Exception {
        mockMvc.perform(post("/load").header("X-Seed-Token", "invalid-token"))
                .andExpect(status().isForbidden());
    }

    private boolean isCreated(Future<Integer> response) {
        try {
            return response.get() == 201;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isOk(Future<Integer> response) {
        try {
            return response.get() == 200;
        } catch (Exception ex) {
            return false;
        }
    }
}
