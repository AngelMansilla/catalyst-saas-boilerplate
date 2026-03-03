package com.catalyst.notification.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration properties for the Notification Service.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "catalyst.notification")
public class NotificationProperties {

    private EmailProperties email = new EmailProperties();
    private AwsProperties aws = new AwsProperties();
    private KafkaTopics kafka = new KafkaTopics();

    @Data
    public static class EmailProperties {
        private String baseUrl = "http://localhost:3000";
        private String logoUrl = "http://localhost:3000/logo.png";
        private String supportEmail = "support@catalyst.com";
        private PasswordResetProperties passwordReset = new PasswordResetProperties();

        @Data
        public static class PasswordResetProperties {
            private int expirationHours = 24;
        }
    }

    @Data
    public static class AwsProperties {
        private SesProperties ses = new SesProperties();

        @Data
        public static class SesProperties {
            private String region = "us-east-1";
            private boolean enabled = false;
            private String accessKeyId;
            private String secretAccessKey;
        }
    }

    @Data
    public static class KafkaTopics {
        private Map<String, String> topics;

        public String getTopic(String key) {
            return topics != null ? topics.get(key) : null;
        }
    }
}
