package vn.com.routex.hub.analytics.processor.infrastructure.kafka.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import vn.com.go.routex.identity.security.log.SystemLog;
import vn.com.routex.hub.analytics.processor.domain.analytics.port.MerchantAnalyticsService;
import vn.com.routex.hub.analytics.processor.infrastructure.kafka.event.TicketIssuedEvent;
import vn.com.routex.hub.analytics.processor.infrastructure.kafka.model.KafkaEventMessage;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.exception.BusinessException;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils.ExceptionUtils;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils.JsonUtils;

import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ErrorConstant.INVALID_INPUT_ERROR;

@Component
@RequiredArgsConstructor
public class AnalyticsKafkaEventConsumer {

    @Value("${spring.kafka.events.ticket-issued:ticket-issued}")
    private String ticketIssuedEvent;

    private final MerchantAnalyticsService merchantAnalyticsService;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @KafkaListener(
            topics = "${spring.kafka.topics.booking}",
            groupId = "${spring.kafka.consumer.group-id:analytics-processor-group}"
    )
    public void consume(String payload, Acknowledgment acknowledgment) {
        sLog.info("[ANALYTICS-KAFKA] Received raw payload: {}", payload);
        try {
            KafkaEventMessage<TicketIssuedEvent> event = JsonUtils.parseToKafkaObject(
                    payload,
                    new TypeReference<>() {}
            );

            if (event == null || event.data() == null) {
                throw new BusinessException(ExceptionUtils.buildResultResponse(INVALID_INPUT_ERROR, "Invalid event payload"));
            }

            if (!ticketIssuedEvent.equals(event.eventType())) {
                sLog.debug("[ANALYTICS-KAFKA] Ignoring non-ticket-issued event: {}", event.eventType());
                acknowledgment.acknowledge();
                return;
            }

            TicketIssuedEvent data = event.data();
            merchantAnalyticsService.processTicketIssuedEvent(data);
            sLog.info("[ANALYTICS-KAFKA] Successfully processed event: {} for bookingId: {}", event.eventType(), data.bookingId());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            sLog.error("[ANALYTICS-KAFKA] Error processing message: {}", e.getMessage(), e);
            // In microservices, we usually acknowledge to prevent blockage or route to DLQ
            acknowledgment.acknowledge();
        }
    }
}
