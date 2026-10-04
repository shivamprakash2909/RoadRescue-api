package com.shivam.roadrescue.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.roadrescue.customer.dto.request.VehicleRequest;
import com.shivam.roadrescue.customer.dto.response.VehicleResponse;
import com.shivam.roadrescue.customer.exception.GlobalExceptionHandler;
import com.shivam.roadrescue.customer.service.VehicleService;
import com.shivam.roadrescue.shared.enums.Role;
import com.shivam.roadrescue.shared.enums.VehicleType;
import com.shivam.roadrescue.shared.exception.DuplicateResourceException;
import com.shivam.roadrescue.shared.exception.ResourceNotFoundException;
import com.shivam.roadrescue.shared.security.CurrentUser;
import com.shivam.roadrescue.shared.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private ObjectMapper objectMapper;
    private UUID userId;
    private UserPrincipal userPrincipal;
    private UUID vehicleId;
    private VehicleResponse vehicleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userId = UUID.randomUUID();
        userPrincipal = UserPrincipal.builder()
                .id(userId)
                .email("customer@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        HandlerMethodArgumentResolver currentUserResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(CurrentUser.class)
                        || parameter.getParameterType().equals(UserPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return userPrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController)
                .setCustomArgumentResolvers(currentUserResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        vehicleId = UUID.randomUUID();
        vehicleResponse = VehicleResponse.builder()
                .id(vehicleId)
                .customerId(UUID.randomUUID())
                .vehicleType(VehicleType.SEDAN)
                .make("Tesla")
                .model("Model 3")
                .year(2023)
                .registrationNumber("EV-8888")
                .color("Red")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/vehicles returns 200 OK with vehicle list")
    void getVehicles_returns200() throws Exception {
        when(vehicleService.getVehiclesByUserId(userId)).thenReturn(List.of(vehicleResponse));

        mockMvc.perform(get("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(vehicleId.toString()))
                .andExpect(jsonPath("$[0].make").value("Tesla"))
                .andExpect(jsonPath("$[0].registrationNumber").value("EV-8888"));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles returns 201 Created on valid vehicle registration")
    void addVehicle_valid_returns201() throws Exception {
        VehicleRequest request = VehicleRequest.builder()
                .vehicleType(VehicleType.SEDAN)
                .make("Tesla")
                .model("Model 3")
                .year(2023)
                .registrationNumber("EV-8888")
                .color("Red")
                .build();

        when(vehicleService.addVehicle(eq(userId), any(VehicleRequest.class))).thenReturn(vehicleResponse);

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(vehicleId.toString()))
                .andExpect(jsonPath("$.make").value("Tesla"));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles returns 409 Conflict on duplicate registration number")
    void addVehicle_duplicate_returns409() throws Exception {
        VehicleRequest request = VehicleRequest.builder()
                .vehicleType(VehicleType.SEDAN)
                .make("Tesla")
                .model("Model 3")
                .year(2023)
                .registrationNumber("EV-8888")
                .color("Red")
                .build();

        when(vehicleService.addVehicle(eq(userId), any(VehicleRequest.class)))
                .thenThrow(new DuplicateResourceException("Vehicle already registered"));

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} returns 404 Not Found if vehicle does not exist or belong to user")
    void getVehicle_notFound_returns404() throws Exception {
        when(vehicleService.getVehicleById(userId, vehicleId))
                .thenThrow(new ResourceNotFoundException("Vehicle not found"));

        mockMvc.perform(get("/api/v1/vehicles/" + vehicleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/v1/vehicles/{id} returns 204 No Content on successful deletion")
    void deleteVehicle_success_returns204() throws Exception {
        doNothing().when(vehicleService).deleteVehicle(userId, vehicleId);

        mockMvc.perform(delete("/api/v1/vehicles/" + vehicleId))
                .andExpect(status().isNoContent());
    }
}
