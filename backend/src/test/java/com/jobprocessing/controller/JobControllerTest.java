package com.jobprocessing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobprocessing.dto.CreateJobRequest;
import com.jobprocessing.dto.JobResponse;
import com.jobprocessing.dto.JobStatsResponse;
import com.jobprocessing.exception.ResourceNotFoundException;
import com.jobprocessing.model.JobPriority;
import com.jobprocessing.model.JobStatus;
import com.jobprocessing.model.JobType;
import com.jobprocessing.service.JobLogService;
import com.jobprocessing.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;

    @MockBean
    private JobLogService logService;

    @Test
    void submitJob_withValidPayload_shouldReturnCreated() throws Exception {
        CreateJobRequest request = new CreateJobRequest();
        request.setName("Daily Analytics Digest");
        request.setType(JobType.REPORT);
        request.setPriority(JobPriority.NORMAL);

        JobResponse response = new JobResponse();
        response.setId("job_abc123");
        response.setName("Daily Analytics Digest");
        response.setType(JobType.REPORT);
        response.setStatus(JobStatus.QUEUED);

        when(jobService.submitJob(any(CreateJobRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("job_abc123"))
                .andExpect(jsonPath("$.data.status").value("QUEUED"));
    }

    @Test
    void submitJob_withMissingName_shouldReturnBadRequest() throws Exception {
        CreateJobRequest request = new CreateJobRequest();
        // missing name
        request.setType(JobType.EMAIL);

        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void getJob_whenNotFound_shouldReturnNotFound() throws Exception {
        when(jobService.getJob("nonexistent")).thenThrow(new ResourceNotFoundException("Job not found with id: nonexistent"));

        mockMvc.perform(get("/api/v1/jobs/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Job not found with id: nonexistent"));
    }

    @Test
    void getJobStats_shouldReturnStats() throws Exception {
        JobStatsResponse stats = new JobStatsResponse();
        stats.setTotalJobs(50);
        stats.setCompletedJobs(45);
        stats.setFailedJobs(5);
        stats.setSuccessRate(90.0);

        when(jobService.getJobStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/jobs/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalJobs").value(50))
                .andExpect(jsonPath("$.data.successRate").value(90.0));
    }
}
