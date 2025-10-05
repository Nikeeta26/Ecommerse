package com.ecommerce.controller;

import com.ecommerce.dto.SubscriptionCheckResponse;
import com.ecommerce.dto.SubscriptionDTO;
import com.ecommerce.dto.SubscriptionRequestDTO;
import com.ecommerce.model.Product;
import com.ecommerce.model.Subscription;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    @GetMapping("/check")
    public ResponseEntity<?> checkProductSubscription(
            @RequestParam Long productId) {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "unauthorized",
                "message", "Authentication required"
            ));
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Check subscription status
        SubscriptionCheckResponse response = subscriptionService.checkProductSubscription(
            userPrincipal.getId(), 
            productId
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<?> createSubscription(@Valid @RequestBody SubscriptionRequestDTO requestDTO) {
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
        Optional<User> userOpt = userRepository.findById(userPrincipal.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "user_not_found",
                "message", "User not found with id: " + userPrincipal.getId()
            ));
        }
        
        User user = userOpt.get();

        try {
            // Convert SubscriptionRequestDTO to SubscriptionDTO
            SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
            subscriptionDTO.setUserId(user.getId());
            
            // Convert each product in the request to SubscriptionProductDTO
            List<SubscriptionDTO.SubscriptionProductDTO> productDTOs = requestDTO.getProducts().stream()
                .map(reqProduct -> {
                    SubscriptionDTO.SubscriptionProductDTO productDTO = new SubscriptionDTO.SubscriptionProductDTO();
                    productDTO.setProductId(reqProduct.getProductId());
                    productDTO.setQuantity(reqProduct.getQuantity());
                    productDTO.setDepositAmount(BigDecimal.ZERO);
                    return productDTO;
                })
                .collect(Collectors.toList());
            
            subscriptionDTO.setProducts(productDTOs);
            
            // Create subscription
            Subscription subscription = subscriptionService.createSubscription(subscriptionDTO, user);
            
            // Prepare detailed response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("subscriptionId", subscription.getId());
            response.put("startDate", subscription.getStartDate());
            response.put("endDate", subscription.getEndDate());
            response.put("status", subscription.isActive() ? "ACTIVE" : "INACTIVE");
            response.put("refillFrequencyDays", subscription.getRefillFrequencyDays());
            response.put("nextRefillDate", subscription.getNextRefillDate());
            
            // Add subscription products information
            List<Map<String, Object>> productsInfo = new ArrayList<>();
            for (Product product : subscription.getProducts()) {
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("productId", product.getId());
                productInfo.put("productName", product.getName());
                productInfo.put("quantity", subscription.getProductQuantities().get(product));
                productsInfo.add(productInfo);
            }
            response.put("products", productsInfo);
            
            // Add helpful messages
            response.put("message", "Subscription created successfully");
            response.put("nextSteps", List.of(
                "You can now purchase refills using this subscription",
                "Your first refill is scheduled for: " + subscription.getNextRefillDate(),
                "Manage your subscription in the 'My Subscriptions' section"
            ));
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "subscription_error",
                "message", "Failed to create subscription: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubscription(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = new User();
            user.setId(userPrincipal.getId());
            user.setEmail(userPrincipal.getEmail());
            Subscription subscription = subscriptionService.getSubscription(id, user);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserSubscriptions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = new User();
            user.setId(userPrincipal.getId());
            user.setEmail(userPrincipal.getEmail());
            List<Subscription> subscriptions = subscriptionService.getUserSubscriptions(user);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelSubscription(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = new User();
            user.setId(userPrincipal.getId());
            user.setEmail(userPrincipal.getEmail());
            subscriptionService.cancelSubscription(id, user);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/request-refill")
    public ResponseEntity<?> requestRefill(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = new User();
            user.setId(userPrincipal.getId());
            user.setEmail(userPrincipal.getEmail());
            boolean success = subscriptionService.requestRefill(id, user);
            if (!success) {
                return ResponseEntity.badRequest().body(Map.of("error", "Refill request failed"));
            }
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}





//
//curl --location 'http://localhost:8080/api/subscriptions' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk1NjcwMzMsImV4cCI6MTc1OTY1MzQzM30.uvU1UB5j0vUxjYumzVWge7CAwoN2WO-Ukgb1c13HzA3kDI9-QBC8lJ72J_lS3Jlg' \
//        --header 'Content-Type: application/json' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk1NjcwMzMsImV4cCI6MTc1OTY1MzQzM30.uvU1UB5j0vUxjYumzVWge7CAwoN2WO-Ukgb1c13HzA3kDI9-QBC8lJ72J_lS3Jlg' \
//        --data '
//        {
//        "products": [
//        {
//        "productId": 24,
//        "quantity": 2
//        }
//        ],
//        "subscriptionName": "My Essential Products",
//        "paymentMethodId": "pm_123456789"
//        }'


//
//
//curl --location 'http://localhost:8080/api/subscriptions/check?productId=24' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk1NzE5NjYsImV4cCI6MTc1OTY1ODM2Nn0.5F-DH-JbpULx19Z_VF08gNYn7oCJY6T7em9bEnz9OcuMArz-A10xfEE-3GGgkRr2' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk1NzE5NjYsImV4cCI6MTc1OTY1ODM2Nn0.5F-DH-JbpULx19Z_VF08gNYn7oCJY6T7em9bEnz9OcuMArz-A10xfEE-3GGgkRr2'