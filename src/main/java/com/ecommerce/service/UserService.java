package com.ecommerce.service;

import com.ecommerce.dto.SignupRequest;
import com.ecommerce.dto.UpdateProfileRequest;
import com.ecommerce.dto.UserProfileResponse;
import com.ecommerce.model.User;

public interface UserService {
    User registerUser(SignupRequest request);

    /**
     * Creates a new admin user after validating a one-time setup code.
     * This is intended for bootstrapping or controlled environments.
     */
    User createAdmin(SignupRequest request, String setupCode);
    
    /**
     * Get user profile by user ID
     */
    UserProfileResponse getUserProfile(Long userId);
    
    /**
     * Update user profile
     */
    UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest request);
    
    /**
     * Update user profile image
     */
    UserProfileResponse updateProfileImage(Long userId, String imageUrl);
}
