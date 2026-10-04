package com.shivam.roadrescue.customer.service.impl;

import com.shivam.roadrescue.customer.dto.request.VehicleRequest;
import com.shivam.roadrescue.customer.dto.response.VehicleResponse;
import com.shivam.roadrescue.customer.entity.CustomerProfile;
import com.shivam.roadrescue.customer.entity.Vehicle;
import com.shivam.roadrescue.customer.mapper.VehicleMapper;
import com.shivam.roadrescue.customer.repository.VehicleRepository;
import com.shivam.roadrescue.customer.service.ActiveBookingChecker;
import com.shivam.roadrescue.customer.service.CustomerService;
import com.shivam.roadrescue.customer.service.VehicleService;
import com.shivam.roadrescue.shared.exception.DuplicateResourceException;
import com.shivam.roadrescue.shared.exception.InvalidStateTransitionException;
import com.shivam.roadrescue.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;
    private final VehicleMapper vehicleMapper;
    private final ActiveBookingChecker activeBookingChecker;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByUserId(UUID userId) {
        log.info("Fetching vehicles for userId={}", userId);
        CustomerProfile profile = customerService.getOrCreateProfileEntity(userId);
        List<Vehicle> vehicles = vehicleRepository.findByCustomerIdOrderByCreatedAtDesc(profile.getId());
        return vehicleMapper.toResponseList(vehicles);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(UUID userId, UUID vehicleId) {
        log.info("Fetching vehicleId={} for userId={}", vehicleId, userId);
        CustomerProfile profile = customerService.getOrCreateProfileEntity(userId);
        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse addVehicle(UUID userId, VehicleRequest request) {
        log.info("Adding vehicle for userId={} with registrationNumber={}", userId, request.getRegistrationNumber());
        CustomerProfile profile = customerService.getOrCreateProfileEntity(userId);

        String regNumber = request.getRegistrationNumber().trim().toUpperCase();
        if (vehicleRepository.existsByRegistrationNumber(regNumber)) {
            throw new DuplicateResourceException("Vehicle with registration number '" + regNumber + "' is already registered");
        }

        Vehicle vehicle = Vehicle.builder()
                .customer(profile)
                .vehicleType(request.getVehicleType())
                .make(request.getMake().trim())
                .model(request.getModel().trim())
                .year(request.getYear())
                .registrationNumber(regNumber)
                .color(request.getColor().trim())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Successfully added vehicle id={} for customer id={}", saved.getId(), profile.getId());
        return vehicleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(UUID userId, UUID vehicleId, VehicleRequest request) {
        log.info("Updating vehicle id={} for userId={}", vehicleId, userId);
        CustomerProfile profile = customerService.getOrCreateProfileEntity(userId);

        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        String regNumber = request.getRegistrationNumber().trim().toUpperCase();
        if (!regNumber.equalsIgnoreCase(vehicle.getRegistrationNumber())
                && vehicleRepository.existsByRegistrationNumberAndCustomerIdNot(regNumber, profile.getId())) {
            throw new DuplicateResourceException("Vehicle with registration number '" + regNumber + "' is already registered");
        }

        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setMake(request.getMake().trim());
        vehicle.setModel(request.getModel().trim());
        vehicle.setYear(request.getYear());
        vehicle.setRegistrationNumber(regNumber);
        vehicle.setColor(request.getColor().trim());

        Vehicle updated = vehicleRepository.save(vehicle);
        log.info("Successfully updated vehicle id={}", updated.getId());
        return vehicleMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteVehicle(UUID userId, UUID vehicleId) {
        log.info("Deleting vehicle id={} for userId={}", vehicleId, userId);
        CustomerProfile profile = customerService.getOrCreateProfileEntity(userId);

        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        if (activeBookingChecker.hasActiveBooking(vehicleId)) {
            log.warn("Cannot delete vehicle id={} because it is associated with an active booking", vehicleId);
            throw new InvalidStateTransitionException("Cannot delete vehicle referenced by an active booking");
        }

        vehicleRepository.delete(vehicle);
        log.info("Successfully deleted vehicle id={}", vehicleId);
    }
}
