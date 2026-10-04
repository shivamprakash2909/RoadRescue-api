package com.shivam.roadrescue.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shivam.roadrescue.shared.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;

    private String email;

    private String fullName;

    @JsonProperty("name")
    public String getName() {
        return fullName;
    }

    private String phone;

    private Role role;

    private boolean enabled;

    private Instant createdAt;
}
