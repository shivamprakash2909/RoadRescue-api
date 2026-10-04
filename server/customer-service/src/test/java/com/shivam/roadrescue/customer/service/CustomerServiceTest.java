package com.shivam.roadrescue.customer.service;

import com.shivam.roadrescue.customer.dto.request.UpdateProfileRequest;
import com.shivam.roadrescue.customer.dto.response.CustomerProfileResponse;
import com.shivam.roadrescue.customer.entity.CustomerProfile;
import com.shivam.roadrescue.customer.mapper.CustomerMapper;
import com.shivam.roadrescue.customer.repository.CustomerProfileRepository;
import com.shivam.roadrescue.customer.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerProfileRepository profileRepository;

    private final CustomerMapper customerMapper = new CustomerMapper();

    private CustomerServiceImpl customerService;

    private UUID userId;
    private CustomerProfile profile;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(profileRepository, customerMapper);

        userId = UUID.randomUUID();
        profile = CustomerProfile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .address("123 Main St")
                .emergencyContactName("Jane Doe")
                .emergencyContactPhone("+1987654321")
                .build();
    }

    @Test
    @DisplayName("getProfile returns existing customer profile")
    void getProfile_existingProfile_returnsProfile() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        CustomerProfileResponse response = customerService.getProfile(userId);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getPhoneNumber()).isEqualTo("+1234567890");
    }

    @Test
    @DisplayName("getProfile creates initial profile if none exists")
    void getProfile_newProfile_createsAndReturnsProfile() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(profileRepository.save(any(CustomerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerProfileResponse response = customerService.getProfile(userId);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        verify(profileRepository).save(any(CustomerProfile.class));
    }

    @Test
    @DisplayName("updateProfile updates and saves profile fields")
    void updateProfile_validRequest_updatesFields() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(CustomerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .phoneNumber("+1122334455")
                .address("456 Elm St")
                .emergencyContactName("Bob Smith")
                .emergencyContactPhone("+1554433221")
                .build();

        CustomerProfileResponse response = customerService.updateProfile(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("Alice");
        assertThat(response.getLastName()).isEqualTo("Smith");
        assertThat(response.getPhoneNumber()).isEqualTo("+1122334455");
        assertThat(response.getAddress()).isEqualTo("456 Elm St");
    }
}
