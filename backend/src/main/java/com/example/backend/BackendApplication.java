package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Main entry point for the Backend Spring Boot application.
 * Extends SpringBootServletInitializer to support WAR deployment.
 */
@SpringBootApplication
public class BackendApplication extends SpringBootServletInitializer {

    /**
     * Standard main method to run the application (e.g., using embedded Tomcat during development).
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Configures the application when deployed as a WAR file to an external servlet container.
     * @param builder SpringApplicationBuilder provided by the container.
     * @return The configured SpringApplicationBuilder.
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BackendApplication.class);
    }
}