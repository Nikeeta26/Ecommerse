package com.ecommerce.service;

import com.ecommerce.dto.SignupRequest;
import com.ecommerce.model.User;

public interface UserService {
    User registerUser(SignupRequest request);

    /**
     * Creates a new admin user after validating a one-time setup code.
     * This is intended for bootstrapping or controlled environments.
     */
    User createAdmin(SignupRequest request, String setupCode);
}
