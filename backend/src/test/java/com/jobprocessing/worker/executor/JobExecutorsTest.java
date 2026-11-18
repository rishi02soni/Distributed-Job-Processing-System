package com.jobprocessing.worker.executor;

import com.jobprocessing.model.Job;
import com.jobprocessing.model.JobType;
import com.jobprocessing.service.JobLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JobExecutorsTest {

    @Mock
    private JobLogService logService;

    private EmailJobExecutor emailExecutor;
    private ReportJobExecutor reportExecutor;
    private DataProcessingJobExecutor dataExecutor;
    private CustomJobExecutor customExecutor;

    @BeforeEach
    void setUp() {
        emailExecutor = new EmailJobExecutor();
        reportExecutor = new ReportJobExecutor();
        dataExecutor = new DataProcessingJobExecutor();
        customExecutor = new CustomJobExecutor();
    }

    @Test
    void emailExecutor_shouldSucceedWithValidPayload() throws Exception {
        Job job = new Job();
        job.setId("job_email_1");
        job.setType(JobType.EMAIL);
        Map<String, Object> payload = new HashMap<>();
        payload.put("to", "user@corp.com");
        payload.put("subject", "Quarterly Brief");
        job.setPayload(payload);

        Map<String, Object> result = emailExecutor.execute(job, logService);

        assertThat(result).isNotNull();
        assertThat(result.get("deliveryStatus")).isEqualTo("SENT");
        assertThat(result.get("recipient")).isEqualTo("user@corp.com");
    }

    @Test
    void emailExecutor_whenSimulateFailure_shouldThrowException() {
        Job job = new Job();
        job.setId("job_email_fail");
        job.setType(JobType.EMAIL);
        Map<String, Object> payload = new HashMap<>();
        payload.put("simulateFailure", true);
        job.setPayload(payload);

        assertThatThrownBy(() -> emailExecutor.execute(job, logService))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated SMTP Connection Timeout");
    }

    @Test
    void reportExecutor_shouldReturnDownloadUrl() throws Exception {
        Job job = new Job();
        job.setId("job_report_1");
        job.setType(JobType.REPORT);
        Map<String, Object> payload = new HashMap<>();
        payload.put("reportName", "Yearly Audit");
        payload.put("format", "CSV");
        job.setPayload(payload);

        Map<String, Object> result = reportExecutor.execute(job, logService);

        assertThat(result).isNotNull();
        assertThat(result.get("format")).isEqualTo("CSV");
        assertThat(result.get("downloadUrl").toString()).contains("reports/");
    }

    @Test
    void dataProcessingExecutor_shouldProcessBatch() throws Exception {
        Job job = new Job();
        job.setId("job_data_1");
        job.setType(JobType.DATA_PROCESSING);
        Map<String, Object> payload = new HashMap<>();
        payload.put("batchSize", 500);
        payload.put("transformation", "AGGREGATE_SALES");
        job.setPayload(payload);

        Map<String, Object> result = dataExecutor.execute(job, logService);

        assertThat(result).isNotNull();
        assertThat(result.get("processedRecords")).isEqualTo(500);
        assertThat(result.get("status")).isEqualTo("SUCCESS");
    }
}
