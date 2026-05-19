package vn.com.routex.hub.analytics.processor.infrastructure.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import vn.com.go.routex.identity.security.base.BaseRequest;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.analytics.processor.domain.outbox.model.OutBoxEvent;
import vn.com.routex.hub.analytics.processor.infrastructure.kafka.event.DomainEvent;
import vn.com.routex.hub.analytics.processor.infrastructure.kafka.model.KafkaEventMessage;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils.JsonUtils;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    public CompletableFuture<Void> publishAsync(OutBoxEvent outboxEvent) {

        String jsonPayload;
        try {
            DomainEvent domainEvent = toDomainEvent(outboxEvent);
            jsonPayload = objectMapper.writeValueAsString(domainEvent);
        } catch (Exception e) {
            sLog.error("[OUTBOX-SERIALIZE-ERROR] eventId={}", outboxEvent.getId(), e);
            return CompletableFuture.failedFuture(e);
        }
        CompletableFuture<SendResult<String, String>> kafkaFuture = kafkaTemplate.send(outboxEvent.getTopic(), outboxEvent.getAggregateId(), jsonPayload);
        return kafkaFuture.thenAccept(result -> {
            RecordMetadata recordMetadata = result.getRecordMetadata();
            sLog.debug("[OUTBOX-PUBLISH-SUCCESS] eventId={}, topic={}, partition={}, offset={}",
                    outboxEvent.getId(),
                    outboxEvent.getTopic(),
                    recordMetadata.partition(),
                    recordMetadata.offset());
        }).exceptionally(ex -> {
            sLog.error(
                    "[OUTBOX-PUBLISH-FAILED] eventId={}, topic={}",
                    outboxEvent.getId(),
                    outboxEvent.getTopic(),
                    ex
            );
            throw new RuntimeException(ex);
        });
    }

    public void publish(OutBoxEvent outboxEvent) {
        try {

            BaseRequest baseRequest = objectMapper.convertValue(outboxEvent.getHeader().get("context"), BaseRequest.class);

            KafkaEventMessage<Object> message =
                    KafkaEventMessage.builder()
                            .requestId(baseRequest.getRequestId())
                            .requestDateTime(baseRequest.getRequestDateTime())
                            .channel(baseRequest.getChannel())
                            .eventId(UUID.randomUUID().toString())
                            .eventType(outboxEvent.getEventType())
                            .aggregateId(outboxEvent.getAggregateId())
                            .source("management-service")
                            .version(1)
                            .occurredAt(OffsetDateTime.now())
                            .data(outboxEvent.getPayload().get("data"))
                            .build();

            String json = JsonUtils.parseToJsonStr(message);

            kafkaTemplate.send(outboxEvent.getTopic(), outboxEvent.getAggregateId(), json);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Kafka publish failed: ", ex);
        }
    }

    private DomainEvent toDomainEvent(OutBoxEvent outBoxEvent) {

        if(outBoxEvent == null) {
            return null;
        }
        return DomainEvent.builder()
                .eventId(outBoxEvent.getId())
                .eventType(outBoxEvent.getEventType())
                .eventKey(outBoxEvent.getEventKey())
                .aggregateId(outBoxEvent.getAggregateId())
                .header(outBoxEvent.getHeader())
                .payload(outBoxEvent.getPayload())
                .build();
    }
}
