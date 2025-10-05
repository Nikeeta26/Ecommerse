// src/main/java/com/ecommerce/service/ReviewService.java
package com.ecommerce.service;
import  com.ecommerce.dto.ReviewDto;
import com.ecommerce.dto.ReviewResponse;
import com.ecommerce.model.User;
import java.util.List;

import com.ecommerce.dto.ReviewDto;
import com.ecommerce.dto.ReviewResponse;
import com.ecommerce.model.User;
import java.util.List;

public interface ReviewService {
    List<ReviewResponse> getProductReviews(Long productId);
    ReviewResponse getReviewById(Long productId, Long reviewId);
    ReviewResponse createReview(Long productId, ReviewDto reviewDto, Long userId);
    ReviewResponse updateReview(Long productId, Long reviewId, ReviewDto reviewDto, Long userId);
    void deleteReview(Long productId, Long reviewId, Long userId);
}