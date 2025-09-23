package com.ecommerce.controller;

import com.ecommerce.model.Admin;
import com.ecommerce.repository.AdminRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @Value("${admin.api.key:}")
    private String adminApiKey;

    private void validateAdminKey(String key) {
        if (adminApiKey == null || adminApiKey.isBlank()) {
            throw new IllegalStateException("Admin API key not configured");
        }
        if (key == null || !key.equals(adminApiKey)) {
            throw new SecurityException("Invalid admin API key");
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<List<Admin>> listAdmins(@RequestHeader("X-ADMIN-KEY") String key) {
        validateAdminKey(key);
        return ResponseEntity.ok(adminRepository.findAll());
        
    }

    public static class CreateAdminRequest {
        @NotBlank
        public String fullName;
        @NotBlank
        @Email
        public String email;
        public String phone;
    }

    @PostMapping("/admins")
    public ResponseEntity<?> createAdmin(@RequestHeader("X-ADMIN-KEY") String key,
                                         @RequestBody CreateAdminRequest req) {
        validateAdminKey(key);
        if (adminRepository.existsByEmail(req.email)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "bad_request",
                    "message", "admin email already exists"
            ));
        }
        Admin a = new Admin();
        a.setFullName(req.fullName);
        a.setEmail(req.email);
        a.setPhone(req.phone);
        a = adminRepository.save(a);
        return ResponseEntity.ok(a);
    }
}
