package com.shivam.roadrescue.customer.repository;

import com.shivam.roadrescue.customer.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    List<Vehicle> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    Optional<Vehicle> findByIdAndCustomerId(UUID id, UUID customerId);

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumberAndCustomerIdNot(String registrationNumber, UUID customerId);
}
