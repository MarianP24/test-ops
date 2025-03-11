package com.hella.ictmanager.controller;

import com.hella.ictmanager.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/shutdown")
    public ResponseEntity<String> shutdownApplication() {
        applicationService.shutdownApplication();
        return ResponseEntity.ok("Application shutdown initiated");
    }

    @PostMapping("/restart")
    public ResponseEntity<String> restartApplication() {
        applicationService.restartApplication();
        return ResponseEntity.ok("Application restart initiated");
    }

    @GetMapping(value = "/logs", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getApplicationLogs() {
        try {
            String logContent = applicationService.getLogContent();
            return ResponseEntity.ok(logContent);
        } catch (Exception e) {
            log.error("Error retrieving log content", e);
            return ResponseEntity.internalServerError()
                    .body("Error retrieving log content: " + e.getMessage());
        }
    }
}