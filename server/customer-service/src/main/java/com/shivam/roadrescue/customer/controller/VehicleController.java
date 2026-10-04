package com.shivam.roadrescue.customer.controller;

import com.shivam.roadrescue.customer.dto.request.VehicleRequest;
import com.shivam.roadrescue.customer.dto.response.VehicleResponse;
import com.shivam.roadrescue.customer.service.VehicleService;
import com.shivam.roadrescue.shared.security.CurrentUser;
import com.shivam.roadrescue.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Vehicle", description = "Customer vehicle management endpoints")
@SecurityRequirement(name = "BearerAuth")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "List customer vehicles", description = "Retrieves all registered vehicles for the authenticated customer")
    public ResponseEntity<List<VehicleResponse>> getVehicles(@CurrentUser UserPrincipal currentUser) {
        log.info("REST request to list vehicles for userId={}", currentUser.getId());
        List<VehicleResponse> vehicles = vehicleService.getVehiclesByUserId(currentUser.getId());
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Register a new vehicle", description = "Registers a vehicle for the authenticated customer")
    public ResponseEntity<VehicleResponse> addVehicle(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody VehicleRequest request) {
        log.info("REST request to add vehicle for userId={}", currentUser.getId());
        VehicleResponse response = vehicleService.addVehicle(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get single vehicle", description = "Retrieves vehicle details by ID (must belong to authenticated customer)")
    public ResponseEntity<VehicleResponse> getVehicle(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id) {
        log.info("REST request to get vehicleId={} for userId={}", id, currentUser.getId());
        VehicleResponse response = vehicleService.getVehicleById(currentUser.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Update vehicle", description = "Updates details of a vehicle owned by authenticated customer")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRequest request) {
        log.info("REST request to update vehicleId={} for userId={}", id, currentUser.getId());
        VehicleResponse response = vehicleService.updateVehicle(currentUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Delete vehicle", description = "Removes a vehicle if it has no active bookings")
    public ResponseEntity<Void> deleteVehicle(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID id) {
        log.info("REST request to delete vehicleId={} for userId={}", id, currentUser.getId());
        vehicleService.deleteVehicle(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
