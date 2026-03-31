package com.jobportal.spring_boot_rest;

import com.jobportal.spring_boot_rest.model.JobPost;
import com.jobportal.spring_boot_rest.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class JobRestController {

    private final JobService service;

    public JobRestController(JobService service) {
        this.service = service;
    }

    @GetMapping(path = "jobPosts", produces = "application/json")
    public ResponseEntity<List<JobPost>> getAllJobs() {
        return ResponseEntity.ok(service.getAllJobs());
    }

    @GetMapping("jobPost/{postId}")
    public ResponseEntity<JobPost> getJob(@PathVariable("postId") int postId) {
        return ResponseEntity.ok(service.getJobOrThrow(postId));
    }

    @PostMapping(path = "jobPost", consumes = "application/xml")
    public ResponseEntity<JobPost> addJob(@RequestBody JobPost jobPost) {
        JobPost created = service.addJob(jobPost);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("jobPost")
    public ResponseEntity<JobPost> updateJob(@RequestBody JobPost jobPost) {
        return ResponseEntity.ok(service.updateJob(jobPost));
    }

    @DeleteMapping("jobPost/{postId}")
    public ResponseEntity<Void> deleteJob(@PathVariable("postId") int postId) {
        service.deleteJob(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("load")
    public ResponseEntity<String> loadData() {
        service.load();
        return ResponseEntity.ok("success");
    }

    @GetMapping("jobPosts/keyword/{keyword}")
    public ResponseEntity<List<JobPost>> searchByKeyword(@PathVariable("keyword") String keyword) {
        return ResponseEntity.ok(service.search(keyword));
    }
}
