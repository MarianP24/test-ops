package com.hella.ictmanager.service.impl;

import com.hella.ictmanager.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationContext applicationContext;

    @Override
    public void shutdownApplication() {
        log.warn("Initiating application shutdown");

        if (applicationContext instanceof ConfigurableApplicationContext configurableContext) {
            CompletableFuture.runAsync(() -> {
                        try {
                            configurableContext.close();
                        } catch (Exception e) {
                            log.error("Error during shutdown", e);
                            throw new RuntimeException("Shutdown failed", e);
                        }
                    }).orTimeout(30, TimeUnit.SECONDS)
                    .exceptionally(throwable -> {
                        log.error("Shutdown timed out or failed", throwable);
                        return null;
                    });
        }
    }

    @Override
    public void restartApplication() {

        log.warn("Initiating application restart");
        ApplicationArguments args = applicationContext.getBean(ApplicationArguments.class);

        Thread restartThread = new Thread(() -> {
            try {
                log.info("Executing application restart");
                ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;

                context.close();

                Thread.sleep(2000);

                Class<?> mainClass = getMainApplicationClass();
                String[] sourceArgs = args.getSourceArgs();

                ConfigurableApplicationContext newContext = null;
                try {
                    SpringApplication app = new SpringApplication(mainClass);
                    app.setRegisterShutdownHook(false);
                    newContext = app.run(sourceArgs);

                    if (newContext != null && newContext.isRunning()) {
                        log.info("Application restarted successfully");
                    } else {
                        log.error("Failed to restart the application");
                    }
                } catch (Exception e) {
                    log.error("Failed to restart application", e);
                    if (newContext != null) {
                        newContext.close();
                    }
                    System.exit(1);
                }
            } catch (Exception e) {
                log.error("Restart failed", e);
                System.exit(1);
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
}