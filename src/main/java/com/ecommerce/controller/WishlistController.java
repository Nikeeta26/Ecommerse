package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.WishlistDto;
import com.ecommerce.model.User;
import com.ecommerce.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse> getWishlist(@AuthenticationPrincipal User user) {
        if (user == null) {
            logger.warn("Unauthorized access attempt to get wishlist");
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required. Please log in."));
        }
        
        try {
            logger.debug("Retrieving wishlist for user {}", user.getId());
            WishlistDto wishlist = wishlistService.getWishlist(user);
            return ResponseEntity.ok(ApiResponse.success("Wishlist retrieved successfully", wishlist));
        } catch (Exception e) {
            logger.error("Error retrieving wishlist for user {}: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving wishlist: " + e.getMessage()));
        }
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse> addToWishlist(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        if (user == null) {
            logger.warn("Unauthorized access attempt to wishlist endpoint");
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required. Please log in."));
        }
        
        try {
            logger.debug("Adding product {} to wishlist for user {}", productId, user.getId());
            WishlistDto wishlist = wishlistService.addToWishlist(user, productId);
            return ResponseEntity.ok(ApiResponse.success("Product added to wishlist", wishlist));
        } catch (Exception e) {
            logger.error("Error adding product {} to wishlist for user {}: {}", 
                productId, user != null ? user.getId() : "unknown", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Unable to add product to wishlist: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> removeFromWishlist(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        if (user == null) {
            logger.warn("Unauthorized attempt to remove item from wishlist");
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required. Please log in."));
        }
        
        try {
            logger.debug("Removing product {} from wishlist for user {}", productId, user.getId());
            wishlistService.removeFromWishlist(user, productId);
            return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist"));
        } catch (Exception e) {
            logger.error("Error removing product {} from wishlist for user {}: {}", 
                productId, user.getId(), e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error removing product from wishlist: " + e.getMessage()));
        }
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse> checkProductInWishlist(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        if (user == null) {
            logger.warn("Unauthorized attempt to check product in wishlist");
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required. Please log in."));
        }
        
        try {
            logger.debug("Checking if product {} is in wishlist for user {}", productId, user.getId());
            boolean isInWishlist = wishlistService.isProductInWishlist(user, productId);
            return ResponseEntity.ok(ApiResponse.success("Product check completed", isInWishlist));
        } catch (Exception e) {
            logger.error("Error checking product {} in wishlist for user {}: {}", 
                productId, user.getId(), e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error checking product in wishlist: " + e.getMessage()));
        }
    }
}
