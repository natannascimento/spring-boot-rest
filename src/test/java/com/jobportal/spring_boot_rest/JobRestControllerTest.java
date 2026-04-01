package com.jobportal.spring_boot_rest;

import com.jobportal.spring_boot_rest.exception.GlobalExceptionHandler;
import com.jobportal.spring_boot_rest.exception.ResourceNotFoundException;
import com.jobportal.spring_boot_rest.model.JobPost;
import com.jobportal.spring_boot_rest.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobRestController.class)
@Import(GlobalExceptionHandler.class)
class JobRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService service;

    @Test
    void getJob_ShouldReturn404WithContract_WhenNotFound() throws Exception {
        when(service.getJobOrThrow(99)).thenThrow(new ResourceNotFoundException("Job post not found for id 99"));

        mockMvc.perform(get("/jobPost/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Job post not found for id 99"))
                .andExpect(jsonPath("$.path").value("/jobPost/99"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void createJob_ShouldReturn201AndLocation_WhenPayloadIsValid() throws Exception {
        JobPost created = new JobPost(10, "Backend Developer", "Build APIs", 3, List.of("Java", "Spring"));
        when(service.addJob(any(JobPost.class))).thenReturn(created);

        String payload = """
                {
                  \"postId\": 10,
                  \"postProfile\": \"Backend Developer\",
                  \"postDesc\": \"Build APIs\",
                  \"reqExperience\": 3,
                  \"postTechStack\": [\"Java\", \"Spring\"]
                }
                """;

        mockMvc.perform(post("/jobPost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/jobPost/10")))
                .andExpect(jsonPath("$.postId").value(10));
    }

    @Test
    void createJob_ShouldReturn400WithValidationDetails_WhenPayloadInvalid() throws Exception {
        String invalidPayload = """
                {
                  \"postId\": 0,
                  \"postProfile\": \"\",
                  \"postDesc\": \"\",
                  \"reqExperience\": -1,
                  \"postTechStack\": []
                }
                """;

        mockMvc.perform(post("/jobPost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void updateJob_ShouldReturn404_WhenResourceMissing() throws Exception {
        when(service.updateJob(any(JobPost.class))).thenThrow(new ResourceNotFoundException("Job post not found for id 99"));

        String payload = """
                {
                  \"postId\": 99,
                  \"postProfile\": \"Dev\",
                  \"postDesc\": \"desc\",
                  \"reqExperience\": 2,
                  \"postTechStack\": [\"Java\"]
                }
                """;

        mockMvc.perform(put("/jobPost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteJob_ShouldReturn204_WhenResourceExists() throws Exception {
        doNothing().when(service).deleteJob(1);

        mockMvc.perform(delete("/jobPost/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteJob_ShouldReturn404_WhenResourceMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Job post not found for id 1")).when(service).deleteJob(1);

        mockMvc.perform(delete("/jobPost/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
