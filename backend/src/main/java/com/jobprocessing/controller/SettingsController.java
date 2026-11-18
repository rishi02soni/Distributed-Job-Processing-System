package com.jobprocessing.controller;

import com.jobprocessing.dto.ApiResponse;
import com.jobprocessing.dto.SettingsDto;
import com.jobprocessing.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@Tag(name = "System Settings", description = "View and modify engine configurations such as retries, timeouts, and concurrency")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    @Operation(summary = "Get system settings", description = "Returns active runtime settings and configurations")
    public ResponseEntity<ApiResponse<SettingsDto>> getSettings() {
        SettingsDto settings = settingsService.getSettings();
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }

    @PutMapping
    @Operation(summary = "Update system settings", description = "Updates runtime configurable parameters (retries, timeouts, backoff)")
    public ResponseEntity<ApiResponse<SettingsDto>> updateSettings(@RequestBody SettingsDto dto) {
        SettingsDto updated = settingsService.updateSettings(dto);
        return ResponseEntity.ok(ApiResponse.ok("Settings updated successfully", updated));
    }
}
