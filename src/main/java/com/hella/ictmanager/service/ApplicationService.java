package com.hella.ictmanager.service;

import org.springframework.stereotype.Service;

@Service
public interface ApplicationService {

    void shutdownApplication();

    void restartApplication();

    String getLogContent();
}