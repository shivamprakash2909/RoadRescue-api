package com.shivam.roadrescue.customer.service;

import com.shivam.roadrescue.customer.dto.request.VehicleRequest;
import com.shivam.roadrescue.customer.dto.response.VehicleResponse;
import com.shivam.roadrescue.customer.entity.CustomerProfile;
import com.shivam.roadrescue.customer.entity.Vehicle;
import com.shivam.roadrescue.customer.mapper.VehicleMapper;
import com.shivam.roadrescue.customer.repository.VehicleRepository;
import com.shivam.roadrescue.customer.service.impl.VehicleServiceImpl;
import com.shivam.roadrescue.shared.enums.VehicleType;
import com.shivam.roadrescue.shared.exception.DuplicateResourceException;
import com.shivam.roadrescue.shared.exception.InvalidStateTransitionException;
import com.shivam.roadrescue.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private ActiveBookingChecker activeBookingChecker;

    private final VehicleMapper vehicleMapper = new VehicleMapper();

    private VehicleServiceImpl vehicleService;

    private UUID userId;
    private CustomerProfile profile;
    private Vehicle vehicle;
    private UUID vehicleId;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository, customerService, vehicleMapper, activeBookingChecker);

        userId = UUID.randomUUID();
        profile = CustomerProfile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .build();

        vehicleId = UUID.randomUUID();
        vehicle = Vehicle.builder()
                .id(vehicleId)
                .customer(profile)
                .vehicleType(VehicleType.SEDAN)
                .make("Toyota")
                .model("Camry")
                .year(2021)
                .registrationNumber("ABC-1234")
                .color("Silver")
                .build();
    }

    @Test
    @DisplayName("getVehiclesByUserId returns all customer vehicles")
    void getVehiclesByUserId_returnsList() {
        when(customerService.getOrCreateProfileEntity(userId)).thenReturn(profile);
        when(vehicleRepository.findByCustomerIdOrderByCreatedAtDesc(profile.getId())).thenReturn(List.of(vehicle));

        List<VehicleResponse> responses = vehicleService.getVehiclesByUserId(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getMake()).isEqualTo("Toyota");
        assertThat(responses.get(0).getRegistrationNumber()).isEqualTo("ABC-1234");
    }

    @Test
    @DisplayName("getVehicleById returns vehicle when owned by user")
    void getVehicleById_owned_returnsVehicle() {
        when(customerService.getOrCreateProfileEntity(userId)).thenReturn(profile);
        when(vehicleRepository.findByIdAndCustomerId(vehicleId, profile.getId())).thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getVehicleById(userId, vehicleId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(vehicleId);
        assertThat(response.getModel()).isEqualTo("Camry");
    }

    @Test
    @DisplayName("getVehicleById throws ResourceNotFoundException if not found or not owned")
    void getVehicleById_notOwned_throwsResourceNotFound() {
        when(customerService.getOrCreateProfileEntity(userId)).thenReturn(profile);
        when(vehicleRepository.findByIdAndCustomerId(vehicleId, profile.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getVehicleById(userId, vehicleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle not found");
    }

    @Test
    @DisplayName("addVehicle successfully saves a new vehicle")
    void addVehicle_valid_savesVehicle() {
        when(customerService.getOrCreateProfileEntity(userId)).thenReturn(profile);
        when(vehicleRepository.existsByRegistrationNumber("XYZ-9999")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleRequest request = VehicleRequest.builder()
                .vehicleType(VehicleType.SUV)
                .make("Honda")
                .model("CR-V")
                .year(2022)
                .registrationNumber("xyz-9999")
                .color("Black")
                .build();

        VehicleResponse response = vehicleService.addVehicle(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.getMake()).isEqualTo("Honda");
        assertThat(response.getRegistrationNumber()).isEqualTo("XYZ-9999");
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("addVehicle throws DuplicateResourceException on duplicate registration")
    void addVehicle_duplicateRegistration_throwsDuplicateException() {
        when(customerService.getOrCreateProfileEntity(userId)).thenReturn(profile);
        when(vehicleRepository.existsByRegistrationNumber("ABC-1234")).thenReturn(true);

        VehicleRequest request = VehicleRequest.builder()
                .vehicleType(VehicleType.SEDAN)
                .make("Toyota")
                .model("Camry")
                .year(2021)
                .registrationNumber("ABC-1234")
                .color("Silver")
                .build();

        assertThatThrownBy(() -> vehicleService.addVehicle(userId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already registered");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("deleteVehicle successfully removes vehicle when no active booking")
    void deleteVehicle_noActiveBooking_deletesVehicle() {
        when(customerService.getOrCreateProfileEntity(userId)).thenReturn(profile);
        when(vehicleRepository.findByIdAndCustomerId(vehicleId, profile.getId())).thenReturn(Optional.of(vehicle));
        when(activeBookingChecker.hasActiveBooking(vehicleId)).thenReturn(false);

        vehicleService.deleteVehicle(userId, vehicleId);

        verify(vehicleRepository).delete(vehicle);
    }

    @Test
    @DisplayName("deleteVehicle blocks removal if vehicle has active booking")
    void deleteVehicle_activeBooking_throwsInvalidState() {
        when(customerService.getOrCreateProfileEntity(userId)).thenReturn(profile);
        when(vehicleRepository.findByIdAndCustomerId(vehicleId, profile.getId())).thenReturn(Optional.of(vehicle));
        when(activeBookingChecker.hasActiveBooking(vehicleId)).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.deleteVehicle(userId, vehicleId))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("active booking");

        verify(vehicleRepository, never()).delete(any(Vehicle.class));
    }
}
