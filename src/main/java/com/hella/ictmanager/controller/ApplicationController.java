package com.hella.ictmanager.controller;

import com.hella.ictmanager.model.ApplicationStatus;
import com.hella.ictmanager.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    public String applicationManagementPage() {
        log.info("Accessing application management page");
        return "applicationService/application-management";
    }

    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<ApplicationStatus> getStatus() {
        return ResponseEntity.ok(applicationService.getStatus());
    }


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
}
