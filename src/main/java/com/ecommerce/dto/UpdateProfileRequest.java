package com.ecommerce.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String phone;
    // Note: Password updates should be handled separately
}
