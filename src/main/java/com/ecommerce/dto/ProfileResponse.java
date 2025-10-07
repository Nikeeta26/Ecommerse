package com.ecommerce.dto;

import com.ecommerce.model.User;
import lombok.Data;

@Data
public class ProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;

    public static ProfileResponse fromUser(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        return response;
    }
}