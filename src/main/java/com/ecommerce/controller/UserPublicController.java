package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserPublicController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getUser(@RequestParam(required = false) String email,
                                     @RequestParam(required = false) String phone) {
        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "bad_request",
                    "message", "Provide email or phone"
            ));
        }
        return userRepository.findByEmailOrPhone(email, phone)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of(
                        "id", u.getId(),
                        "name", u.getName(),
                        "email", u.getEmail(),
                        "phone", u.getPhoneNumber()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
