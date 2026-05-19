package vn.com.routex.hub.analytics.processor.domain.analytics.port;

import vn.com.routex.hub.analytics.processor.infrastructure.kafka.event.TicketIssuedEvent;

public interface MerchantAnalyticsService {
    void processTicketIssuedEvent(TicketIssuedEvent event);
}
