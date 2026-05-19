package vn.com.routex.hub.analytics.processor.domain.outbox;

public enum OutBoxEventStatus {
    PENDING,
    PROCESSED,
    COMPLETED,
    FAILED
}
