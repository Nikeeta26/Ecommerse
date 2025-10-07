package com.ecommerce.controller;

import com.ecommerce.dto.UpdateProfileRequest;
import com.ecommerce.model.User;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("User not authenticated");
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            // Get the UserPrincipal
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            logger.info("Updating profile for user ID: {}", userPrincipal.getId());

            // Delegate to service and get updated user
            User updatedUser = userService.updateProfile(userPrincipal.getId(), request);

            // Create a response with only the updated fields
            Map<String, Object> response = new HashMap<>();
            if (request.getFullName() != null) {
                response.put("fullName", updatedUser.getFullName());
            }
            if (request.getEmail() != null) {
                response.put("email", updatedUser.getEmail());
            }
            if (request.getPhone() != null) {
                response.put("phone", updatedUser.getPhone());
            }

            return ResponseEntity.ok(response);

        } catch (ClassCastException e) {
            logger.error("Authentication principal is not of expected type", e);
            return ResponseEntity.status(500).body(
                    Map.of("error", "Authentication error",
                            "details", "Unexpected principal type: " +
                                    (authentication != null && authentication.getPrincipal() != null ?
                                            authentication.getPrincipal().getClass().getName() : "null")));
        } catch (RuntimeException e) {
            logger.error("Error updating profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(500).body(
                    Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
        }
    }
}