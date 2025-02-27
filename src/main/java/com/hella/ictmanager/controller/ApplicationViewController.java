package com.hella.ictmanager.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/application")
public class ApplicationViewController {

    @GetMapping
    public String applicationManagementPage() {
        log.info("Accessing application management page");
        return "applicationService/application-management";
    }
}