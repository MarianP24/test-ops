package com.hella.ictmanager.service.impl;

import com.hella.ictmanager.model.ApplicationStatus;
import com.hella.ictmanager.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {
    private static final String PHASE_SHUTTING_DOWN = "SHUTTING_DOWN";
    private static final String PHASE_SERVER_RESTARTING = "SERVER_RESTARTING";
    private static final String PHASE_STARTING_UP = "STARTING_UP";
    private static final String PHASE_COMPLETED = "COMPLETED";

    private final ApplicationContext applicationContext;
    private ApplicationStatus currentStatus = null;
    private final AtomicBoolean isRestarting = new AtomicBoolean(false);

    @Override
    public ApplicationStatus getStatus() {
        if (!isRestarting.get()) {
            return new ApplicationStatus(
                    PHASE_COMPLETED,
                    100,
                    "Application is running normally",
                    true
            );
        }

        return currentStatus != null ? currentStatus :
                new ApplicationStatus(PHASE_SHUTTING_DOWN, 0, "Initializing restart process", false);
    }

    @Override
    public void shutdownApplication() {
        log.warn("Initiating application shutdown");
        updateStatus(PHASE_SHUTTING_DOWN, 0, "Initiating shutdown sequence");

        Thread shutdownThread = new Thread(() -> {
            try {
                updateStatus(PHASE_SHUTTING_DOWN, 50, "Closing application context");
                ConfigurableApplicationContext configurableContext =
                        (ConfigurableApplicationContext) applicationContext;

                configurableContext.close();
                updateStatus(PHASE_COMPLETED, 100, "Shutdown completed", true);

                System.exit(0);
            } catch (Exception e) {
                log.error("Error during shutdown", e);
                updateStatus(PHASE_COMPLETED, 100, "Shutdown failed: " + e.getMessage(), true);
                System.exit(1);
            }
        }, "ShutdownThread");

        shutdownThread.setDaemon(false);
        shutdownThread.start();
    }

    @Override
    public void restartApplication() {
        if (!isRestarting.compareAndSet(false, true)) {
            log.warn("Restart already in progress");
            return;
        }

        log.warn("Initiating application restart");
        updateStatus(PHASE_SHUTTING_DOWN, 0, "Initiating restart sequence");
        ApplicationArguments args = applicationContext.getBean(ApplicationArguments.class);

        Thread restartThread = new Thread(() -> {
            try {
                log.info("Executing application restart");
                ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;

                updateStatus(PHASE_SHUTTING_DOWN, 25, "Closing current context");
                context.close();

                updateStatus(PHASE_SERVER_RESTARTING, 50, "Waiting for context to close");
                Thread.sleep(2000);

                Class<?> mainClass = getMainApplicationClass();
                String[] sourceArgs = args.getSourceArgs();

                updateStatus(PHASE_STARTING_UP, 75, "Initializing new context");
                ConfigurableApplicationContext newContext = null;
                try {
                    SpringApplication app = new SpringApplication(mainClass);
                    app.setRegisterShutdownHook(false);
                    newContext = app.run(sourceArgs);

                    if (newContext != null && newContext.isRunning()) {
                        updateStatus(PHASE_COMPLETED, 100, "Application restarted successfully", true);
                        log.info("Application restarted successfully");
                    } else {
                        updateStatus(PHASE_COMPLETED, 100, "Failed to restart the application", true);
                        log.error("Failed to restart the application");
                    }
                } catch (Exception e) {
                    log.error("Failed to restart application", e);
                    if (newContext != null) {
                        newContext.close();
                    }
                    updateStatus(PHASE_COMPLETED, 100, "Restart failed: " + e.getMessage(), true);
                    System.exit(1);
                }
            } catch (Exception e) {
                log.error("Restart failed", e);
                updateStatus(PHASE_COMPLETED, 100, "Restart failed: " + e.getMessage(), true);
                System.exit(1);
            } finally {
                isRestarting.set(false);
            }
        }, "RestartThread");

        restartThread.setDaemon(false);
        restartThread.start();
    }

    private Class<?> getMainApplicationClass() {
        try {
            return Class.forName("com.hella.ictmanager.IctManagerApplication");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Main application class not found", e);
        }
    }

    private void updateStatus(String phase, int progress, String message) {
        updateStatus(phase, progress, message, false);
    }

    private void updateStatus(String phase, int progress, String message, boolean completed) {
        currentStatus = new ApplicationStatus(phase, progress, message, completed);
        log.info("Status updated: {}", currentStatus);
    }
}