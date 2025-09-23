package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.SignupRequest;
import com.ecommerce.dto.AdminSignupRequest;
import com.ecommerce.model.Admin;
import com.ecommerce.repository.AdminRepository;
import com.ecommerce.security.JwtTokenProvider;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminRepository adminRepository;

    @Value("${admin.setup.code:}")
    private String adminSetupCode;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getIdentifier(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        AuthResponse resp = new AuthResponse(jwt);
        resp.setUserId(principal.getId());
        resp.setFullName(principal.getName());
        resp.setEmail(principal.getEmail());
        resp.setRole(principal.getAuthorities().iterator().next().getAuthority());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        userService.registerUser(signUpRequest);
        return ResponseEntity.ok(new ApiResponse(true, "User registered successfully"));
    }

    // Secure admin creation (admins table only) using setup code from application.properties
    @PostMapping("/signup-admin")
    public ResponseEntity<?> registerAdmin(
            @RequestHeader(value = "X-ADMIN-SETUP-CODE", required = false) String setupCodeHeader,
            @Valid @RequestBody AdminSignupRequest request) {
        String setupCode = (request.getSetupCode() != null && !request.getSetupCode().isBlank())
                ? request.getSetupCode() : setupCodeHeader;

        if (setupCode == null || setupCode.isBlank()) {
            throw new IllegalArgumentException("Admin setup code is required (header X-ADMIN-SETUP-CODE or body.setupCode)");
        }
        if (adminSetupCode == null || adminSetupCode.isBlank()) {
            throw new IllegalStateException("Admin setup code not configured");
        }
        if (!adminSetupCode.equals(setupCode)) {
            throw new SecurityException("Invalid admin setup code");
        }

        // Create only in admins table (no user row)
        adminRepository.findByEmail(request.getEmail()).ifPresent(a -> {
            throw new IllegalArgumentException("Admin email already exists");
        });

        Admin a = new Admin();
        a.setFullName(request.getFullName());
        a.setEmail(request.getEmail());
        a.setPhone(request.getPhone());
        adminRepository.save(a);

        return ResponseEntity.ok(new ApiResponse(true, "Admin created in admins table"));
    }
}
