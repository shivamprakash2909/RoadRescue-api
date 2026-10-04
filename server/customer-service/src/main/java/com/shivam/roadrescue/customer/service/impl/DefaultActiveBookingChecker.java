package com.shivam.roadrescue.customer.service.impl;

import com.shivam.roadrescue.customer.service.ActiveBookingChecker;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DefaultActiveBookingChecker implements ActiveBookingChecker {

    @Override
    public boolean hasActiveBooking(UUID vehicleId) {
        // Defaults to false until booking-service integration in Phase 5
        return false;
    }
}
