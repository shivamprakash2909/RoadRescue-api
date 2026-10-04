package com.shivam.roadrescue.customer.service;

import com.shivam.roadrescue.customer.dto.request.UpdateProfileRequest;
import com.shivam.roadrescue.customer.dto.response.CustomerProfileResponse;
import com.shivam.roadrescue.customer.entity.CustomerProfile;

import java.util.UUID;

public interface CustomerService {

    CustomerProfileResponse getProfile(UUID userId);

    CustomerProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    CustomerProfile getOrCreateProfileEntity(UUID userId);
}
