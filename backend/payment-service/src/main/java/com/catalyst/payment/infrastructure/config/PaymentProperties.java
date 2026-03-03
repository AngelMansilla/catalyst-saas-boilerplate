package com.catalyst.payment.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration properties for the Payment Service.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "catalyst.payment")
public class PaymentProperties {

    private StripeProperties stripe = new StripeProperties();
    private TrialProperties trial = new TrialProperties();
    private Map<String, TierProperties> tiers;
    private KafkaTopics kafka = new KafkaTopics();

    @Data
    public static class StripeProperties {
        private String apiKey;
        private String webhookSecret;
        private String publicKey;
    }

    @Data
    public static class TrialProperties {
        private int durationDays = 14;
    }

    @Data
    public static class TierProperties {
        private String monthlyPriceId;
        private String annualPriceId;
        private double monthlyAmount;
        private double annualAmount;
    }

    @Data
    public static class KafkaTopics {
        private Map<String, String> topics;

        public String getTopic(String key) {
            return topics != null ? topics.get(key) : null;
        }
    }
}
