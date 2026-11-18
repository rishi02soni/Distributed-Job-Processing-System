package com.jobprocessing.worker.executor;

import com.jobprocessing.model.Job;
import com.jobprocessing.model.JobType;
import com.jobprocessing.service.JobLogService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class EmailJobExecutor implements JobExecutor {

    @Override
    public JobType getSupportedType() {
        return JobType.EMAIL;
    }

    @Override
    public Map<String, Object> execute(Job job, JobLogService logService) throws Exception {
        Map<String, Object> payload = job.getPayload();
        String to = (String) payload.getOrDefault("to", "recipient@example.com");
        String subject = (String) payload.getOrDefault("subject", "Automated Notification");
        Boolean failSimulated = Boolean.TRUE.equals(payload.get("simulateFailure"));

        logService.logInfo(job.getId(), "Validating email recipient: " + to);
        Thread.sleep(300); // Simulate network overhead

        if (failSimulated) {
            logService.logError(job.getId(), "Simulated SMTP connection timeout to mail exchange", null);
            throw new RuntimeException("Simulated SMTP Connection Timeout for recipient " + to);
        }

        logService.logInfo(job.getId(), "Rendering HTML template with subject: '" + subject + "'");
        Thread.sleep(400);

        String messageId = "msg_" + UUID.randomUUID().toString().substring(0, 8);
        logService.logInfo(job.getId(), "Email dispatched successfully. Message-ID: " + messageId);

        Map<String, Object> result = new HashMap<>();
        result.put("messageId", messageId);
        result.put("recipient", to);
        result.put("deliveryStatus", "SENT");
        result.put("smtpResponse", "250 2.0.0 OK queued as " + messageId);
        return result;
    }
}
