package vn.com.routex.hub.analytics.processor.infrastructure.kafka.model;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record KafkaEventMessage<T> (
        String requestId,
        String requestDateTime,
        String channel,
        String eventId,
        String eventType,
        String aggregateId,
        String source,
        Integer version,
        OffsetDateTime occurredAt,
        T data
) {
}
