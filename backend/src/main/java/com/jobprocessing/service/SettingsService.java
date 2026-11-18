package com.jobprocessing.service;

import com.jobprocessing.dto.SettingsDto;

public interface SettingsService {

    void initDefaults();

    SettingsDto getSettings();

    SettingsDto updateSettings(SettingsDto dto);

    int getIntSetting(String key, int fallback);

    long getLongSetting(String key, long fallback);

    double getDoubleSetting(String key, double fallback);
}
