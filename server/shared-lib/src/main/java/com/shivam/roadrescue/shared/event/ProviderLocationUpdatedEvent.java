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
public class ProviderLocationUpdatedEvent extends BaseEvent {
    private UUID providerId;
    private UUID bookingId;
    private Double latitude;
    private Double longitude;
    private Integer estimatedArrivalMinutes;
}
