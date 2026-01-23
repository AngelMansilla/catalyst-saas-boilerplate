package com.catalyst.shared.infrastructure.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Custom health indicator for Kafka broker connectivity.
 * Checks if the service can connect to Kafka brokers and describe the cluster.
 * Only active when KafkaAdmin bean is available.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(KafkaAdmin.class)
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    @Override
    public Health health() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            
            // Get cluster ID and controller
            String clusterId = clusterResult.clusterId().get(5, TimeUnit.SECONDS);
            int nodeCount = clusterResult.nodes().get(5, TimeUnit.SECONDS).size();
            
            log.debug("Kafka health check: UP - Cluster ID: {}, Nodes: {}", clusterId, nodeCount);
            return Health.up()
                    .withDetail("kafka", "Connected")
                    .withDetail("clusterId", clusterId)
                    .withDetail("nodeCount", nodeCount)
                    .withDetail("status", "Cluster accessible")
                    .build();
        } catch (Exception e) {
            log.error("Kafka health check failed: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("kafka", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
