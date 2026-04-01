package com.jobportal.spring_boot_rest.repo;

import com.jobportal.spring_boot_rest.model.JobPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobRepoIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("jobs_repo_db")
            .withUsername("jobs")
            .withPassword("jobs");

    @Autowired
    private JobRepo repo;

    @DynamicPropertySource
    static void registerDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Test
    void shouldPersistAndRetrieveElementCollection() {
        JobPost post = new JobPost(100, "Backend", "Java APIs", 3, List.of("Java", "Spring", "SQL"));
        repo.save(post);

        JobPost loaded = repo.findById(100).orElseThrow();

        assertEquals(3, loaded.getPostTechStack().size());
        assertEquals("Java", loaded.getPostTechStack().getFirst());
    }

    @Test
    void shouldSearchByKeywordInProfileOrDescription() {
        repo.save(new JobPost(101, "Frontend Engineer", "React and CSS", 2, List.of("React")));
        repo.save(new JobPost(102, "Data Engineer", "Python ETL", 4, List.of("Python")));

        List<JobPost> results = repo.findByPostProfileContainingOrPostDescContaining("React", "React");

        assertEquals(1, results.size());
        assertEquals(101, results.getFirst().getPostId());
        assertFalse(results.isEmpty());
    }
}
