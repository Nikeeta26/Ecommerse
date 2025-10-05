package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.ReviewDto;
import com.ecommerce.dto.ReviewResponse;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // Get all reviews for a product
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }

    // Get a single review by ID
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        ReviewResponse review = reviewService.getReviewById(productId, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review retrieved successfully", review));
    }

    // Create a new review
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewDto reviewDto,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        ReviewResponse review = reviewService.createReview(productId, reviewDto, userPrincipal.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review added successfully", review));
    }

    // Update a review
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewDto reviewDto,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        ReviewResponse updatedReview = reviewService.updateReview(
                productId,
                reviewId,
                reviewDto,
                userPrincipal.getId()
        );
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", updatedReview));
    }

    // Delete a review
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        reviewService.deleteReview(productId, reviewId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}




//
//curl --location 'http://localhost:8080/api/orders/buy-now' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NTQ3MzcsImV4cCI6MTc1OTc0MTEzN30.p0gvyZH6bJOBsfqSkeDGfhHRkuKh6QsRtaBBnGxtndhiscV4REWd4CPzOb5104wx' \
//        --header 'Content-Type: application/json' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NTU1MDIsImV4cCI6MTc1OTc0MTkwMn0.CIlRYn83qwjY3FA5Y0DhX6Hp46WiGZi5nsSM8fYszcKtb5uoUWSGB8kk8-uyrm1N' \
//        --data '{
//        "productId": 29,
//        "quantity": 1,
//        "shippingAddressId": 19
//        }'


//curl --location 'http://localhost:8080/api/addresses' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk1NzE5NjYsImV4cCI6MTc1OTY1ODM2Nn0.5F-DH-JbpULx19Z_VF08gNYn7oCJY6T7em9bEnz9OcuMArz-A10xfEE-3GGgkRr2' \
//        --header 'Content-Type: application/json' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtYW5qdUBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NTE5ODUsImV4cCI6MTc1OTczODM4NX0.HaX82MAmEa7C6N4Kb3U_pKD5xfH4LZ0pIyEsmIW11jsewylXhyEJnbgaJZMsK49h' \
//        --data '{ "addressLine1": "456 Another Street",
//        "city": "New York",
//        "state": "NY",
//        "postalCode": "10002",
//        "country": "USA",
//        "isDefault": true,
//        "addressType": "HOME",
//        "isActive": true}'



//
//
//curl --location 'http://localhost:8080/api/products/30/reviews' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NTQ3MzcsImV4cCI6MTc1OTc0MTEzN30.p0gvyZH6bJOBsfqSkeDGfhHRkuKh6QsRtaBBnGxtndhiscV4REWd4CPzOb5104wx' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NTU1MDIsImV4cCI6MTc1OTc0MTkwMn0.CIlRYn83qwjY3FA5Y0DhX6Hp46WiGZi5nsSM8fYszcKtb5uoUWSGB8kk8-uyrm1N'