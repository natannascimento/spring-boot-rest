package com.jobportal.spring_boot_rest.service;

import com.jobportal.spring_boot_rest.exception.ResourceNotFoundException;
import com.jobportal.spring_boot_rest.model.JobPost;
import com.jobportal.spring_boot_rest.repo.JobRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class JobService {

    private final JobRepo repo;

    public JobService(JobRepo repo) {
        this.repo = repo;
    }

    public JobPost addJob(JobPost jobPost) {
        return repo.save(jobPost);
    }

    public List<JobPost> getAllJobs() {
        return repo.findAll();
    }

    public JobPost getJobOrThrow(int postId) {
        return repo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found for id " + postId));
    }

    public JobPost updateJob(JobPost jobPost) {
        int postId = jobPost.getPostId();
        if (!repo.existsById(postId)) {
            throw new ResourceNotFoundException("Job post not found for id " + postId);
        }
        return repo.save(jobPost);
    }

    public void deleteJob(int postId) {
        if (!repo.existsById(postId)) {
            throw new ResourceNotFoundException("Job post not found for id " + postId);
        }
        repo.deleteById(postId);
    }

    public void load() {
        List<JobPost> jobs = new ArrayList<>(Arrays.asList(

                new JobPost(1, "Java Developer", "Must have good experience in core Java and advanced Java", 2,
                        List.of("Core Java", "J2EE", "Spring Boot", "Hibernate")),


                new JobPost(2, "Frontend Developer", "Experience in building responsive web applications using React", 3,
                        List.of("HTML", "CSS", "JavaScript", "React")),


                new JobPost(3, "Data Scientist", "Strong background in machine learning and data analysis", 4,
                        List.of("Python", "Machine Learning", "Data Analysis")),


                new JobPost(4, "Network Engineer", "Design and implement computer networks for efficient data communication", 5,
                        List.of("Networking", "Cisco", "Routing", "Switching")),


                new JobPost(5, "Mobile App Developer", "Experience in mobile app development for iOS and Android", 3,
                        List.of("iOS Development", "Android Development", "Mobile App"))
        ));

        repo.saveAll(jobs);
    }

    public List<JobPost> search(String keyword) {
        return repo.findByPostProfileContainingOrPostDescContaining(keyword, keyword);
    }
}
