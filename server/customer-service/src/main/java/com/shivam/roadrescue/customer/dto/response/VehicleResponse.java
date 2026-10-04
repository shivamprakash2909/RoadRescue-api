package com.shivam.roadrescue.customer.dto.response;

import com.shivam.roadrescue.shared.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {

    private UUID id;
    private UUID customerId;
    private VehicleType vehicleType;
    private String make;
    private String model;
    private Integer year;
    private String registrationNumber;
    private String color;
    private Instant createdAt;
    private Instant updatedAt;
}
