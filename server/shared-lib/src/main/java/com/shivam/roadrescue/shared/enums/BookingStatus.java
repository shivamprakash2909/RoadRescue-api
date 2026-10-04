package com.shivam.roadrescue.shared.enums;

public enum BookingStatus {
    REQUESTED,
    SEARCHING,
    PROVIDER_ASSIGNED,
    PROVIDER_ACCEPTED,
    PROVIDER_EN_ROUTE,
    ARRIVED,
    SERVICE_STARTED,
    SERVICE_COMPLETED,
    PAYMENT_PENDING,
    COMPLETED,
    CANCELLED;

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
