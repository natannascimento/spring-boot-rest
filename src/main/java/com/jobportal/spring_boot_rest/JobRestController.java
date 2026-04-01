package com.jobportal.spring_boot_rest;

import com.jobportal.spring_boot_rest.model.JobPost;
import com.jobportal.spring_boot_rest.service.JobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
public class JobRestController {

    private final JobService service;
    private final boolean seedEnabled;
    private final String seedToken;

    public JobRestController(
            JobService service,
            @Value("${app.seed.enabled:false}") boolean seedEnabled,
            @Value("${app.seed.token:}") String seedToken) {
        this.service = service;
        this.seedEnabled = seedEnabled;
        this.seedToken = seedToken;
    }

    @GetMapping(path = "jobPosts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<JobPost>> getAllJobs() {
        return ResponseEntity.ok(service.getAllJobs());
    }

    @GetMapping(path = "jobPost/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobPost> getJob(@PathVariable("postId") int postId) {
        return ResponseEntity.ok(service.getJobOrThrow(postId));
    }

    @PostMapping(path = "jobPost", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobPost> addJob(@Valid @RequestBody JobPost jobPost) {
        JobPost created = service.addJob(jobPost);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{postId}")
                .buildAndExpand(created.getPostId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping(path = "jobPost", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobPost> updateJob(@Valid @RequestBody JobPost jobPost) {
        return ResponseEntity.ok(service.updateJob(jobPost));
    }

    @DeleteMapping("jobPost/{postId}")
    public ResponseEntity<Void> deleteJob(@PathVariable("postId") int postId) {
        service.deleteJob(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("load")
    public ResponseEntity<String> loadData(@RequestHeader(value = "X-Seed-Token", required = false) String providedSeedToken) {
        validateSeedAccess(providedSeedToken);

        boolean created = service.loadIfNeeded();
        if (created) {
            return ResponseEntity.status(HttpStatus.CREATED).body("seed loaded");
        }

        return ResponseEntity.ok("seed already loaded");
    }

    @GetMapping(path = "jobPosts/keyword/{keyword}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<JobPost>> searchByKeyword(@PathVariable("keyword") String keyword) {
        return ResponseEntity.ok(service.search(keyword));
    }

    private void validateSeedAccess(String providedSeedToken) {
        if (!seedEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endpoint not available");
        }

        if (!seedToken.isBlank() && !seedToken.equals(providedSeedToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid seed token");
        }
    }
}
