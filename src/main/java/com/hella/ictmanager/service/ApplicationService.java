package com.hella.ictmanager.service;

import com.hella.ictmanager.model.ApplicationStatus;
import org.springframework.stereotype.Service;

@Service
public interface ApplicationService {

    ApplicationStatus getStatus();

    void shutdownApplication();

    void restartApplication();

}