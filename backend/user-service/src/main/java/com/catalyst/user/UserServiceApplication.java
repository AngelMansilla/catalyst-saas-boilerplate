package com.catalyst.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Catalyst User Service.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>User registration (email/password)</li>
 *   <li>Credential validation for NextAuth</li>
 *   <li>Social user synchronization (Google, GitHub)</li>
 *   <li>Password reset functionality</li>
 *   <li>User profile management</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@SpringBootApplication(scanBasePackages = {
    "com.catalyst.user",
    "com.catalyst.shared"
})
@ConfigurationPropertiesScan
@EnableScheduling
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

