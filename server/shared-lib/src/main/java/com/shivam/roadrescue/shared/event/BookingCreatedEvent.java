package com.shivam.roadrescue.shared.event;

import com.shivam.roadrescue.shared.enums.ServiceType;
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
public class BookingCreatedEvent extends BaseEvent {
    private UUID bookingId;
    private UUID customerId;
    private UUID vehicleId;
    private ServiceType serviceType;
    private String problemDescription;
    private Double latitude;
    private Double longitude;
}
