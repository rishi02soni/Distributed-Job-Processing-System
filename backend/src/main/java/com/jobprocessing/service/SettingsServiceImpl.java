package com.jobprocessing.service;

import com.jobprocessing.dto.SettingsDto;
import com.jobprocessing.model.SystemSettings;
import com.jobprocessing.repository.SystemSettingsRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SettingsServiceImpl implements SettingsService {

    private final SystemSettingsRepository settingsRepository;

    @Value("${app.worker.default-max-retries:3}")
    private int defaultMaxRetries;

    @Value("${app.worker.backoff-base-ms:2000}")
    private long defaultBackoffBaseMs;

    @Value("${app.worker.backoff-multiplier:2.0}")
    private double defaultBackoffMultiplier;

    @Value("${app.worker.job-timeout-seconds:60}")
    private int defaultJobTimeoutSeconds;

    @Value("${app.worker.concurrency:4}")
    private int defaultWorkerConcurrency;

    public SettingsServiceImpl(SystemSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Override
    @PostConstruct
    public void initDefaults() {
        initSettingIfAbsent("defaultMaxRetries", defaultMaxRetries, "Default retry attempts for jobs", false);
        initSettingIfAbsent("backoffBaseMs", defaultBackoffBaseMs, "Base backoff duration in milliseconds", false);
        initSettingIfAbsent("backoffMultiplier", defaultBackoffMultiplier, "Exponential backoff multiplier", false);
        initSettingIfAbsent("jobTimeoutSeconds", defaultJobTimeoutSeconds, "Maximum job execution duration before timeout", false);
        initSettingIfAbsent("workerConcurrency", defaultWorkerConcurrency, "Worker thread concurrency limit", true);
        initSettingIfAbsent("streamRetentionMaxLen", 10000L, "Maximum entries retained in Redis streams", false);
        initSettingIfAbsent("dashboardRefreshIntervalMs", 3000, "Frontend dashboard live poll interval in ms", false);
    }

    private void initSettingIfAbsent(String key, Object defaultValue, String description, boolean requiresRestart) {
        if (settingsRepository.findByKey(key).isEmpty()) {
            settingsRepository.save(new SystemSettings(key, defaultValue, description, requiresRestart));
        }
    }

    @Override
    public SettingsDto getSettings() {
        SettingsDto dto = new SettingsDto();
        dto.setDefaultMaxRetries(getIntSetting("defaultMaxRetries", defaultMaxRetries));
        dto.setBackoffBaseMs(getLongSetting("backoffBaseMs", defaultBackoffBaseMs));
        dto.setBackoffMultiplier(getDoubleSetting("backoffMultiplier", defaultBackoffMultiplier));
        dto.setJobTimeoutSeconds(getIntSetting("jobTimeoutSeconds", defaultJobTimeoutSeconds));
        dto.setWorkerConcurrency(getIntSetting("workerConcurrency", defaultWorkerConcurrency));
        dto.setStreamRetentionMaxLen(getLongSetting("streamRetentionMaxLen", 10000L));
        dto.setDashboardRefreshIntervalMs(getIntSetting("dashboardRefreshIntervalMs", 3000));
        return dto;
    }

    @Override
    public SettingsDto updateSettings(SettingsDto dto) {
        updateSetting("defaultMaxRetries", dto.getDefaultMaxRetries());
        updateSetting("backoffBaseMs", dto.getBackoffBaseMs());
        updateSetting("backoffMultiplier", dto.getBackoffMultiplier());
        updateSetting("jobTimeoutSeconds", dto.getJobTimeoutSeconds());
        updateSetting("workerConcurrency", dto.getWorkerConcurrency());
        updateSetting("streamRetentionMaxLen", dto.getStreamRetentionMaxLen());
        updateSetting("dashboardRefreshIntervalMs", dto.getDashboardRefreshIntervalMs());
        return getSettings();
    }

    private void updateSetting(String key, Object val) {
        settingsRepository.findByKey(key).ifPresentOrElse(s -> {
            s.setValue(val);
            s.setUpdatedAt(Instant.now());
            settingsRepository.save(s);
        }, () -> settingsRepository.save(new SystemSettings(key, val, key, false)));
    }

    @Override
    public int getIntSetting(String key, int fallback) {
        return settingsRepository.findByKey(key)
                .map(s -> ((Number) s.getValue()).intValue())
                .orElse(fallback);
    }

    @Override
    public long getLongSetting(String key, long fallback) {
        return settingsRepository.findByKey(key)
                .map(s -> ((Number) s.getValue()).longValue())
                .orElse(fallback);
    }

    @Override
    public double getDoubleSetting(String key, double fallback) {
        return settingsRepository.findByKey(key)
                .map(s -> ((Number) s.getValue()).doubleValue())
                .orElse(fallback);
    }
}
