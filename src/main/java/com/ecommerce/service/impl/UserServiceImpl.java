package com.ecommerce.service.impl;

import com.ecommerce.dto.SignupRequest;
import com.ecommerce.model.User;
import com.ecommerce.model.Admin;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.AdminRepository;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.setup.code:}")
    private String adminSetupCode;

    @Autowired
    private AdminRepository adminRepository;

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
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.UserRole.ROLE_USER);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User createAdmin(SignupRequest request, String setupCode) {
        if (adminSetupCode == null || adminSetupCode.isBlank()) {
            throw new IllegalStateException("Admin setup code is not configured");
        }
        if (setupCode == null || !adminSetupCode.equals(setupCode)) {
            throw new SecurityException("Invalid admin setup code");
        }

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

        User admin = new User();
        admin.setFullName(request.getFullName());
        admin.setEmail(request.getEmail());
        admin.setPhone(request.getPhone());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole(User.UserRole.ROLE_ADMIN);
        User saved = userRepository.save(admin);

        // Mirror into admins table for platform admin registry if not present
        adminRepository.findByEmail(saved.getEmail()).orElseGet(() -> {
            Admin a = new Admin();
            a.setFullName(saved.getFullName());
            a.setEmail(saved.getEmail());
            a.setPhone(saved.getPhone());
            return adminRepository.save(a);
        });

        return saved;
    }
}
