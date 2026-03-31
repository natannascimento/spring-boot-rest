package com.jobportal.spring_boot_rest.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job_post")
public class JobPost {

    @Id
    @Column(name = "post_id")
    private int postId;

    @Column(name = "post_profile", nullable = false)
    private String postProfile;

    @Column(name = "post_desc", nullable = false, length = 2000)
    private String postDesc;

    @Column(name = "req_experience", nullable = false)
    private int reqExperience;

    @ElementCollection
    @CollectionTable(name = "job_post_tech_stack", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tech_stack", nullable = false)
    @OrderColumn(name = "order_idx")
    private List<String> postTechStack;

}
