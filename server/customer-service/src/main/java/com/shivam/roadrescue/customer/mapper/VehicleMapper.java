package com.shivam.roadrescue.customer.mapper;

import com.shivam.roadrescue.customer.dto.response.VehicleResponse;
import com.shivam.roadrescue.customer.entity.Vehicle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VehicleMapper {

    public VehicleResponse toResponse(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .customerId(vehicle.getCustomer() != null ? vehicle.getCustomer().getId() : null)
                .vehicleType(vehicle.getVehicleType())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .registrationNumber(vehicle.getRegistrationNumber())
                .color(vehicle.getColor())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }

    public List<VehicleResponse> toResponseList(List<Vehicle> vehicles) {
        if (vehicles == null) {
            return List.of();
        }
        return vehicles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
