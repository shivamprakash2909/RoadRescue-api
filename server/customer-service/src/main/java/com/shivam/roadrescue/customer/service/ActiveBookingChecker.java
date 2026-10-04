package com.shivam.roadrescue.customer.service;

import java.util.UUID;

public interface ActiveBookingChecker {
    boolean hasActiveBooking(UUID vehicleId);
}
