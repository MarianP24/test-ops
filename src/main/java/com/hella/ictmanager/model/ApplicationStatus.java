package com.hella.ictmanager.model;

public record ApplicationStatus(
        String phase,
        int progress,
        String message,
        boolean completed
) {}
