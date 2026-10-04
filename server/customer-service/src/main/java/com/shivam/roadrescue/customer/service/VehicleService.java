package com.shivam.roadrescue.customer.service;

import com.shivam.roadrescue.customer.dto.request.VehicleRequest;
import com.shivam.roadrescue.customer.dto.response.VehicleResponse;

import java.util.List;
import java.util.UUID;

public interface VehicleService {

    List<VehicleResponse> getVehiclesByUserId(UUID userId);

    VehicleResponse getVehicleById(UUID userId, UUID vehicleId);

    VehicleResponse addVehicle(UUID userId, VehicleRequest request);

    VehicleResponse updateVehicle(UUID userId, UUID vehicleId, VehicleRequest request);

    void deleteVehicle(UUID userId, UUID vehicleId);
}
