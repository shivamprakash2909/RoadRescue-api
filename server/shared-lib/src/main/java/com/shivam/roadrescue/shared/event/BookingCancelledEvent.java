package com.shivam.roadrescue.shared.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BookingCancelledEvent extends BaseEvent {
    private UUID bookingId;
    private UUID customerId;
    private UUID providerId;
    private String cancelledBy;
    private String reason;
}
