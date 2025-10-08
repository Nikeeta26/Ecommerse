package com.ecommerce.controller;

import com.ecommerce.dto.ReviewDto;
import com.ecommerce.dto.ReviewRequest;
import com.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest reviewRequest) {
        String email = userDetails.getUsername();
        ReviewDto reviewDto = reviewService.createReview(email, reviewRequest);
        return new ResponseEntity<>(reviewDto, HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest reviewRequest) {
        String email = userDetails.getUsername();
        ReviewDto updatedReview = reviewService.updateReview(email, reviewId, reviewRequest);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId) {
        String email = userDetails.getUsername();
        reviewService.deleteReview(email, reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByProduct(@PathVariable Long productId) {
        List<ReviewDto> reviews = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<List<ReviewDto>> getUserReviews(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<ReviewDto> reviews = reviewService.getReviewsByUser(email);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable Long reviewId) {
        ReviewDto review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/product/{productId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long productId) {
        Double averageRating = reviewService.getAverageRatingByProduct(productId);
        return ResponseEntity.ok(averageRating);
    }
}




//curl --location 'http://localhost:8080/api/reviews' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJuZXcuZW1haWxAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTk5MzY1NDYsImV4cCI6MTc2MDAyMjk0Nn0.FPHrQLFQkmBxTPzl1fU-s6dwAPu9-TDc1RbkNGyJPNlTgSemAllJhTOj0lS1xelX' \
//        --header 'Content-Type: application/json' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJuZXcuZW1haWxAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTk5MzY1NDYsImV4cCI6MTc2MDAyMjk0Nn0.FPHrQLFQkmBxTPzl1fU-s6dwAPu9-TDc1RbkNGyJPNlTgSemAllJhTOj0lS1xelX' \
//        --data '{
//        "productId": 34,
//        "rating": 5,
//        "comment": "Excellent product!"
//        }'


//curl --location --request PUT 'http://localhost:8080/api/reviews/1' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJuZXcuZW1haWxAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTk5MzY1NDYsImV4cCI6MTc2MDAyMjk0Nn0.FPHrQLFQkmBxTPzl1fU-s6dwAPu9-TDc1RbkNGyJPNlTgSemAllJhTOj0lS1xelX' \
//        --header 'Content-Type: application/json' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJuZXcuZW1haWxAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTk5MzY1NDYsImV4cCI6MTc2MDAyMjk0Nn0.FPHrQLFQkmBxTPzl1fU-s6dwAPu9-TDc1RbkNGyJPNlTgSemAllJhTOj0lS1xelX' \
//        --data '{
//        "productId": 34,
//        "rating": 4,
//        "comment": "Very good, but could be better"
//        }'


//
//curl --location 'http://localhost:8080/api/reviews/product/34' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJuZXcuZW1haWxAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTk5MzY1NDYsImV4cCI6MTc2MDAyMjk0Nn0.FPHrQLFQkmBxTPzl1fU-s6dwAPu9-TDc1RbkNGyJPNlTgSemAllJhTOj0lS1xelX' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJuZXcuZW1haWxAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTk5MzY1NDYsImV4cCI6MTc2MDAyMjk0Nn0.FPHrQLFQkmBxTPzl1fU-s6dwAPu9-TDc1RbkNGyJPNlTgSemAllJhTOj0lS1xelX'




//curl --location 'http://localhost:8080/api/reviews/my-reviews' \
//        --header 'Authorization: Bearer YOUR_JWT_TOKEN'


//curl --location 'http://localhost:8080/api/reviews/1' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJuZXcuZW1haWxAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTk5MzY1NDYsImV4cCI6MTc2MDAyMjk0Nn0.FPHrQLFQkmBxTPzl1fU-s6dwAPu9-TDc1RbkNGyJPNlTgSemAllJhTOj0lS1xelX'