package vn.com.routex.hub.analytics.processor.infrastructure.kafka.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
public class KafkaEventProperties {

    private Map<String, String> events = new HashMap<>();
    private Map<String, String> topics = new HashMap<>();
    private Retry retry = new Retry();


    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts = 3;
        private long backOffMs = 1000;
        private double backOffMultiplier = 2.0;
    }
}
