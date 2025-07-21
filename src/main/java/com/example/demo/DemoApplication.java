package com.example.demo;

// Import necessary Spring Boot classes
import org.springframework.boot.SpringApplication; // For running the Spring Boot application
import org.springframework.boot.autoconfigure.SpringBootApplication; // For enabling Spring Boot auto-configuration

// Main application class annotated with @SpringBootApplication
@SpringBootApplication // Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan
public class DemoApplication {

    // Main method to start the Spring Boot application
    public static void main(String[] args) {
        // Run the Spring Boot application by passing the DemoApplication class and command-line arguments
        SpringApplication.run(DemoApplication.class, args);
    }
}
