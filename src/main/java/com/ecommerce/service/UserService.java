package com.ecommerce.service;

import com.ecommerce.dto.SignupRequest;
import com.ecommerce.model.User;

public interface UserService {
    User registerUser(SignupRequest request);
}
