package com.shivam.roadrescue.customer.controller;

import com.shivam.roadrescue.customer.dto.request.UpdateProfileRequest;
import com.shivam.roadrescue.customer.dto.response.CustomerProfileResponse;
import com.shivam.roadrescue.customer.service.CustomerService;
import com.shivam.roadrescue.shared.security.CurrentUser;
import com.shivam.roadrescue.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Customer", description = "Customer profile management endpoints")
@SecurityRequirement(name = "BearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get current customer profile", description = "Retrieves the authenticated customer's profile")
    public ResponseEntity<CustomerProfileResponse> getProfile(@CurrentUser UserPrincipal currentUser) {
        log.info("REST request to get profile for userId={}", currentUser.getId());
        CustomerProfileResponse response = customerService.getProfile(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Update current customer profile", description = "Updates profile information for the authenticated customer")
    public ResponseEntity<CustomerProfileResponse> updateProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("REST request to update profile for userId={}", currentUser.getId());
        CustomerProfileResponse response = customerService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }
}
