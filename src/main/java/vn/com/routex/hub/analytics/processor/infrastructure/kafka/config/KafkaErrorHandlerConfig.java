package vn.com.routex.hub.analytics.processor.infrastructure.kafka.config;


import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.analytics.processor.infrastructure.kafka.properties.KafkaEventProperties;

@Configuration
@RequiredArgsConstructor
public class KafkaErrorHandlerConfig {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaEventProperties kafkaEventProperties;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());
    @Bean
    public DefaultErrorHandler defaultErrorHandler() {
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(kafkaEventProperties.getRetry().getBackOffMs());
        backOff.setMultiplier(kafkaEventProperties.getRetry().getBackOffMultiplier());
        backOff.setMaxAttempts(kafkaEventProperties.getRetry().getMaxAttempts());


        DefaultErrorHandler errorHandler = getDefaultErrorHandler(backOff);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                sLog.error("[KAFKA-RETRY] attempt={} topic={} partition={} offset={} key={} exception={}",
                        deliveryAttempt,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        ex == null ? null : ex.getClass().getName(),
                        ex)
        );
        return errorHandler;
    }

    private @NonNull DefaultErrorHandler getDefaultErrorHandler(ExponentialBackOff backOff) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    sLog.error("[KAFKA-RECOVER] Send record to DLT topic={} partition={} offset={} key={} exception={}",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            record.key(),
                            ex.getClass().getName(),
                            ex);
                    return new TopicPartition("bus-dead-letter-queue", record.partition());
                }
        );

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
