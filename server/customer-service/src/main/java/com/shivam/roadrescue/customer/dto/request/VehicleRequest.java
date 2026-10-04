package com.shivam.roadrescue.customer.dto.request;

import com.shivam.roadrescue.shared.enums.VehicleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "Make is required")
    @Size(min = 1, max = 100, message = "Make must be between 1 and 100 characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(min = 1, max = 100, message = "Model must be between 1 and 100 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1950, message = "Year must be at least 1950")
    @Max(value = 2030, message = "Year cannot exceed 2030")
    private Integer year;

    @NotBlank(message = "Registration number is required")
    @Size(min = 2, max = 50, message = "Registration number must be between 2 and 50 characters")
    private String registrationNumber;

    @NotBlank(message = "Color is required")
    @Size(min = 1, max = 50, message = "Color must be between 1 and 50 characters")
    private String color;
}
