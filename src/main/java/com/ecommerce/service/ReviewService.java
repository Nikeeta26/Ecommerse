package com.ecommerce.service;

import com.ecommerce.dto.ReviewDto;
import com.ecommerce.dto.ReviewRequest;

import java.util.List;

public interface ReviewService {
    ReviewDto createReview(String email, ReviewRequest reviewRequest);
    ReviewDto updateReview(String email, Long reviewId, ReviewRequest reviewRequest);
    void deleteReview(String email, Long reviewId);
    List<ReviewDto> getReviewsByProduct(Long productId);
    List<ReviewDto> getReviewsByUser(String email);
    ReviewDto getReviewById(Long reviewId);
    Double getAverageRatingByProduct(Long productId);
}
