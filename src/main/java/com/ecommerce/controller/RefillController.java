package com.ecommerce.controller;

import com.ecommerce.dto.RefillRequestDTO;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.RefillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/refills")
@RequiredArgsConstructor
public class RefillController {

    private final RefillService refillService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> requestRefill(
            @Valid @RequestBody RefillRequestDTO refillRequest) {
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "unauthorized",
                "message", "Authentication required"
            ));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Get the full user from database
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userPrincipal.getId()));
            
        try {
            // Check if user has an active subscription
            if (!refillService.canRequestRefill(refillRequest, user)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_request",
                    "message", "You don't have an active subscription or invalid request"
                ));
            }
            
            // Process the refill request
            Object result = refillService.processRefill(refillRequest, user);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "refill_processing_error",
                "message", "Failed to process refill: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<?> getRefillHistory(@PathVariable Long subscriptionId) {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "unauthorized",
                "message", "Authentication required"
            ));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "user_not_found",
                "message", "User not found"
            ));
        }
        
        return ResponseEntity.ok(refillService.getRefillHistory(subscriptionId, userOpt.get()));
    }

    @GetMapping("/products")
    public ResponseEntity<?> getRefillableProducts() {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "unauthorized",
                "message", "Authentication required"
            ));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "user_not_found",
                "message", "User not found"
            ));
        }
        
        return ResponseEntity.ok(refillService.getRefillableProducts(userOpt.get()));
    }
}


//curl --location 'http://localhost:8080/api/refills' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NDMyMjIsImV4cCI6MTc1OTcyOTYyMn0.Qz2TVHRh3JA850HW3_0PXIwb57V5KgJYu0MGuB0NePyVVWy-kF2TpAbJ14Ege29K' \
//        --header 'Content-Type: application/json' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtYW5qdUBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NTE5ODUsImV4cCI6MTc1OTczODM4NX0.HaX82MAmEa7C6N4Kb3U_pKD5xfH4LZ0pIyEsmIW11jsewylXhyEJnbgaJZMsK49h' \
//        --data '{
//        "subscriptionId": 8,
//        "shippingAddressId": 19,
//        "items": [
//        {
//        "productId": 33,
//        "quantity": 2
//        }
//        ]
//        }'


//
//curl --location 'http://localhost:8080/api/subscriptions' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NDMyMjIsImV4cCI6MTc1OTcyOTYyMn0.Qz2TVHRh3JA850HW3_0PXIwb57V5KgJYu0MGuB0NePyVVWy-kF2TpAbJ14Ege29K' \
//        --header 'Content-Type: application/json' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtYW5qdUBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NTE5ODUsImV4cCI6MTc1OTczODM4NX0.HaX82MAmEa7C6N4Kb3U_pKD5xfH4LZ0pIyEsmIW11jsewylXhyEJnbgaJZMsK49h' \
//        --data '
//
//
//        {
//        "products": [
//        {
//        "productId": 32,
//        "quantity": 2
//        }
//        ],
//        "subscriptionName": "Hair Care Subscription",
//        "startDate": "2025-10-05",
//        "refillFrequencyDays": 30,
//        "shippingAddressId": 19,
//        "paymentMethodId": "pm_123456789"
//        }'


//
//curl --location 'http://localhost:8080/api/subscriptions' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NDMyMjIsImV4cCI6MTc1OTcyOTYyMn0.Qz2TVHRh3JA850HW3_0PXIwb57V5KgJYu0MGuB0NePyVVWy-kF2TpAbJ14Ege29K' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtYW5qdUBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NDM4MTgsImV4cCI6MTc1OTczMDIxOH0.39c6_hNjQ0UGl_X5DtYEXHCxndhuoz7CSk_djFlZio2D5xBXTLpfb4HyWq0YWe9u'