package com.ecommerce.service.impl;

import com.ecommerce.dto.SignupRequest;
import com.ecommerce.dto.UpdateProfileRequest;
import com.ecommerce.dto.UserProfileResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.User;
import com.ecommerce.model.Admin;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.AdminRepository;
import com.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private Environment environment;

    @Value("${admin.setup.code:}")
    private String adminSetupCode;

    @Override
    @Transactional
    public User registerUser(SignupRequest request) {
        if ((request.getEmail() == null || request.getEmail().isBlank()) &&
                (request.getPhone() == null || request.getPhone().isBlank())) {
            throw new IllegalArgumentException("Either email or phone must be provided");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank() && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank() && userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone is already in use");
        }

        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User createAdmin(SignupRequest request, String setupCode) {
        // Get the admin setup code from environment
        String adminSetupCode = environment.getProperty("app.admin.setup-code");

        if (adminSetupCode == null || adminSetupCode.isEmpty() || !adminSetupCode.equals(setupCode)) {
            throw new SecurityException("Invalid setup code");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User admin = new User();
        admin.setName(request.getFullName());
        admin.setEmail(request.getEmail());
        admin.setPhoneNumber(request.getPhone());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole(User.UserRole.ROLE_ADMIN);  // Set admin role
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("Fetching profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserProfileResponse.fromUser(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update user fields
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());

        // Save the updated user
        User updatedUser = userRepository.save(user);
        log.info("Successfully updated profile for user ID: {}", userId);

        return UserProfileResponse.fromUser(updatedUser);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfileImage(Long userId, String imageUrl) {
        // Find the existing user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update the profile image URL
        user.setProfileImageUrl(imageUrl);
        user.setUpdatedAt(LocalDateTime.now());

        // Save the updated user
        User updatedUser = userRepository.save(user);

        // Convert to response
        return UserProfileResponse.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .phoneNumber(updatedUser.getPhoneNumber())
                .profileImageUrl(updatedUser.getProfileImageUrl())
                .createdAt(updatedUser.getCreatedAt())
                .updatedAt(updatedUser.getUpdatedAt())
                .build();
    }
}