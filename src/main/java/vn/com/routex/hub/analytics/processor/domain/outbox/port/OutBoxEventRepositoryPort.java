package vn.com.routex.hub.analytics.processor.domain.outbox.port;
import vn.com.routex.hub.analytics.processor.domain.outbox.model.OutBoxEvent;

import java.time.OffsetDateTime;
import java.util.List;

public interface OutBoxEventRepositoryPort {
    void save(OutBoxEvent outboxEvent);

    void markAsProcessed(List<String> processedIds, OffsetDateTime now);
    void markAsFailed(List<String> failedIds, OffsetDateTime now);
    List<OutBoxEvent> lockPendingBatch(int i);
}
