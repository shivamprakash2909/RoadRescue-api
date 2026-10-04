package com.shivam.roadrescue.customer.mapper;

import com.shivam.roadrescue.customer.dto.response.CustomerProfileResponse;
import com.shivam.roadrescue.customer.entity.CustomerProfile;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerProfileResponse toResponse(CustomerProfile profile) {
        if (profile == null) {
            return null;
        }
        return CustomerProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactPhone(profile.getEmergencyContactPhone())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
