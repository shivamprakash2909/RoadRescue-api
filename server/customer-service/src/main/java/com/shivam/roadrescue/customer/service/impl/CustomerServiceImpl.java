package com.shivam.roadrescue.customer.service.impl;

import com.shivam.roadrescue.customer.dto.request.UpdateProfileRequest;
import com.shivam.roadrescue.customer.dto.response.CustomerProfileResponse;
import com.shivam.roadrescue.customer.entity.CustomerProfile;
import com.shivam.roadrescue.customer.mapper.CustomerMapper;
import com.shivam.roadrescue.customer.repository.CustomerProfileRepository;
import com.shivam.roadrescue.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerProfileRepository profileRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(UUID userId) {
        log.info("Fetching customer profile for userId={}", userId);
        CustomerProfile profile = getOrCreateProfileEntity(userId);
        return customerMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public CustomerProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        log.info("Updating customer profile for userId={}", userId);
        CustomerProfile profile = getOrCreateProfileEntity(userId);

        profile.setFirstName(request.getFirstName().trim());
        profile.setLastName(request.getLastName().trim());
        profile.setPhoneNumber(request.getPhoneNumber().trim());
        profile.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        profile.setEmergencyContactName(request.getEmergencyContactName() != null ? request.getEmergencyContactName().trim() : null);
        profile.setEmergencyContactPhone(request.getEmergencyContactPhone() != null ? request.getEmergencyContactPhone().trim() : null);

        CustomerProfile saved = profileRepository.save(profile);
        log.info("Successfully updated customer profile id={} for userId={}", saved.getId(), userId);
        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CustomerProfile getOrCreateProfileEntity(UUID userId) {
        return profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("No profile found for userId={}, creating initial empty profile", userId);
                    CustomerProfile newProfile = CustomerProfile.builder()
                            .userId(userId)
                            .firstName("")
                            .lastName("")
                            .phoneNumber("")
                            .build();
                    return profileRepository.save(newProfile);
                });
    }
}
