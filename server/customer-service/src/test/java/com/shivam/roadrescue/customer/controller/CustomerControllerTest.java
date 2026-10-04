package com.shivam.roadrescue.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.roadrescue.customer.dto.request.UpdateProfileRequest;
import com.shivam.roadrescue.customer.dto.response.CustomerProfileResponse;
import com.shivam.roadrescue.customer.exception.GlobalExceptionHandler;
import com.shivam.roadrescue.customer.service.CustomerService;
import com.shivam.roadrescue.shared.enums.Role;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private ObjectMapper objectMapper;
    private UUID userId;
    private UserPrincipal userPrincipal;
    private CustomerProfileResponse profileResponse;

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

        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setCustomArgumentResolvers(currentUserResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        profileResponse = CustomerProfileResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .address("123 Maple St")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/customers/profile returns 200 OK with profile")
    void getProfile_returns200() throws Exception {
        when(customerService.getProfile(userId)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/v1/customers/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/profile returns 200 OK on valid update")
    void updateProfile_valid_returns200() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .phoneNumber("+1987654321")
                .address("456 Oak St")
                .build();

        CustomerProfileResponse updated = CustomerProfileResponse.builder()
                .id(profileResponse.getId())
                .userId(userId)
                .firstName("Jane")
                .lastName("Doe")
                .phoneNumber("+1987654321")
                .address("456 Oak St")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(customerService.updateProfile(eq(userId), any(UpdateProfileRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/customers/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.phoneNumber").value("+1987654321"));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/profile returns 400 Bad Request on invalid phone number")
    void updateProfile_invalidPhone_returns400() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .phoneNumber("invalid-phone")
                .build();

        mockMvc.perform(put("/api/v1/customers/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
