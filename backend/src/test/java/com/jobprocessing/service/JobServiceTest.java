package com.jobprocessing.service;

import com.jobprocessing.dto.CreateJobRequest;
import com.jobprocessing.dto.JobResponse;
import com.jobprocessing.dto.JobStatsResponse;
import com.jobprocessing.exception.InvalidJobStateException;
import com.jobprocessing.exception.ResourceNotFoundException;
import com.jobprocessing.model.Job;
import com.jobprocessing.model.JobPriority;
import com.jobprocessing.model.JobStatus;
import com.jobprocessing.model.JobType;
import com.jobprocessing.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private MongoOperations mongoTemplate;

    @Mock
    private QueueService queueService;

    @Mock
    private JobLogService logService;

    @Mock
    private SettingsService settingsService;

    @Mock
    private WorkerService workerService;

    @InjectMocks
    private JobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        lenient().when(settingsService.getIntSetting(eq("defaultMaxRetries"), anyInt())).thenReturn(3);
        lenient().when(settingsService.getIntSetting(eq("jobTimeoutSeconds"), anyInt())).thenReturn(60);
    }

    @Test
    void submitJob_withValidRequest_shouldSaveAndPublish() {
        CreateJobRequest request = new CreateJobRequest();
        request.setName("Send Welcome Email");
        request.setType(JobType.EMAIL);
        request.setPriority(JobPriority.HIGH);
        Map<String, Object> payload = new HashMap<>();
        payload.put("to", "test@domain.com");
        request.setPayload(payload);

        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = jobService.submitJob(request, null);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotBlank();
        assertThat(response.getName()).isEqualTo("Send Welcome Email");
        assertThat(response.getType()).isEqualTo(JobType.EMAIL);
        assertThat(response.getStatus()).isEqualTo(JobStatus.QUEUED);
        assertThat(response.getPriority()).isEqualTo(JobPriority.HIGH);

        verify(jobRepository).save(any(Job.class));
        verify(queueService).publishJob(any(Job.class));
        verify(logService).logInfo(anyString(), contains("Job submitted"));
    }

    @Test
    void submitJob_withExistingIdempotencyKey_shouldReturnExistingWithoutPublishing() {
        String key = "idem-key-12345";
        Job existingJob = new Job();
        existingJob.setId("job_existing_99");
        existingJob.setName("Pre-existing Job");
        existingJob.setType(JobType.REPORT);
        existingJob.setIdempotencyKey(key);
        existingJob.setStatus(JobStatus.COMPLETED);

        when(jobRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existingJob));

        CreateJobRequest request = new CreateJobRequest();
        request.setName("Pre-existing Job");
        request.setType(JobType.REPORT);
        request.setIdempotencyKey(key);

        JobResponse response = jobService.submitJob(request, null);

        assertThat(response.getId()).isEqualTo("job_existing_99");
        assertThat(response.getStatus()).isEqualTo(JobStatus.COMPLETED);

        verify(jobRepository, never()).save(any(Job.class));
        verify(queueService, never()).publishJob(any(Job.class));
    }

    @Test
    void getJob_whenNotFound_shouldThrowResourceNotFoundException() {
        when(jobRepository.findById("job_unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJob("job_unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Job not found");
    }

    @Test
    void retryJob_whenJobFailed_shouldResetAndReEnqueue() {
        Job failedJob = new Job();
        failedJob.setId("job_failed_1");
        failedJob.setStatus(JobStatus.FAILED);
        failedJob.setAttemptCount(3);

        when(jobRepository.findById("job_failed_1")).thenReturn(Optional.of(failedJob));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = jobService.retryJob("job_failed_1");

        assertThat(response.getStatus()).isEqualTo(JobStatus.QUEUED);
        assertThat(response.getAttemptCount()).isEqualTo(0);

        verify(queueService).publishJob(failedJob);
    }

    @Test
    void retryJob_whenJobRunning_shouldThrowInvalidJobStateException() {
        Job runningJob = new Job();
        runningJob.setId("job_running_1");
        runningJob.setStatus(JobStatus.RUNNING);

        when(jobRepository.findById("job_running_1")).thenReturn(Optional.of(runningJob));

        assertThatThrownBy(() -> jobService.retryJob("job_running_1"))
                .isInstanceOf(InvalidJobStateException.class)
                .hasMessageContaining("Cannot retry job");
    }

    @Test
    void cancelJob_whenJobCompleted_shouldThrowInvalidJobStateException() {
        Job completedJob = new Job();
        completedJob.setId("job_comp_1");
        completedJob.setStatus(JobStatus.COMPLETED);

        when(jobRepository.findById("job_comp_1")).thenReturn(Optional.of(completedJob));

        assertThatThrownBy(() -> jobService.cancelJob("job_comp_1"))
                .isInstanceOf(InvalidJobStateException.class)
                .hasMessageContaining("Cannot cancel an already COMPLETED job");
    }
}
