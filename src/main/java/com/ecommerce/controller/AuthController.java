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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getIdentifier(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // Set HTTP-only cookie
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        AuthResponse resp = new AuthResponse("Authentication successful");

        resp.setUserId(principal.getId());
        resp.setFullName(principal.getName());
        resp.setEmail(principal.getEmail());
        resp.setRole(principal.getAuthorities().iterator().next().getAuthority());
        resp.setToken(jwt);
        resp.setTokenType("Bearer");
        resp.setAccessToken(jwt); // For backward compatibility
        return ResponseEntity.ok(resp);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        userService.registerUser(signUpRequest);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully"));
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

        // Validate either email or phone is provided
        if ((request.getEmail() == null || request.getEmail().isBlank()) &&
            (request.getPhone() == null || request.getPhone().isBlank())) {
            throw new IllegalArgumentException("Either email or phone must be provided for admin");
        }

        // Uniqueness checks in admins table
        if (request.getEmail() != null && !request.getEmail().isBlank() && adminRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Admin email already exists");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank() && adminRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Admin phone already exists");
        }

        // Persist only in admins table with hashed password
        Admin a = new Admin();
        a.setFullName(request.getFullName());
        a.setEmail(request.getEmail());
        a.setPhone(request.getPhone());
        a.setPassword(passwordEncoder.encode(request.getPassword()));
        adminRepository.save(a);

        return ResponseEntity.ok(ApiResponse.success("Admin created successfully"));
    }
}
