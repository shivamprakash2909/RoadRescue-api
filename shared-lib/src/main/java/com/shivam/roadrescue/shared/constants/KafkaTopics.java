package com.shivam.roadrescue.shared.constants;

public final class KafkaTopics {

    private KafkaTopics() {
        // Prevent instantiation
    }

    public static final String BOOKING_CREATED = "booking-created-topic";
    public static final String PROVIDER_SEARCH_STARTED = "provider-search-started-topic";
    public static final String PROVIDER_ASSIGNED = "provider-assigned-topic";
    public static final String PROVIDER_ACCEPTED = "provider-accepted-topic";
    public static final String PROVIDER_REJECTED = "provider-rejected-topic";
    public static final String PROVIDER_EN_ROUTE = "provider-en-route-topic";
    public static final String PROVIDER_ARRIVED = "provider-arrived-topic";
    public static final String SERVICE_STARTED = "service-started-topic";
    public static final String SERVICE_COMPLETED = "service-completed-topic";
    public static final String INVOICE_CREATED = "invoice-created-topic";
    public static final String PAYMENT_INITIATED = "payment-initiated-topic";
    public static final String PAYMENT_SUCCEEDED = "payment-succeeded-topic";
    public static final String PAYMENT_FAILED = "payment-failed-topic";
    public static final String BOOKING_CANCELLED = "booking-cancelled-topic";
    public static final String PROVIDER_AVAILABILITY_CHANGED = "provider-availability-changed-topic";
    public static final String PROVIDER_LOCATION_UPDATED = "provider-location-updated-topic";
}
