// src/main/java/com/ecommerce/service/impl/ReviewServiceImpl.java
package com.ecommerce.service.impl;

import com.ecommerce.dto.ReviewDto;
import com.ecommerce.dto.ReviewResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.ReviewMapper;
import com.ecommerce.model.Product;
import com.ecommerce.model.Review;
import com.ecommerce.model.User;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        return reviewRepository.findByProductId(productId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long productId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Review not found for product with id: " + productId);
        }

        return reviewMapper.toDto(review);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(Long productId, ReviewDto reviewDto, Long userId) {
        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user has already reviewed this product
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You have already reviewed this product"
            );
        }

        // Create and save the review
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long productId, Long reviewId, ReviewDto reviewDto, Long userId) {
        // Find the review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Verify the review belongs to the specified product
        if (!review.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Review not found for product with id: " + productId);
        }

        // Verify the review belongs to the user
        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You don't have permission to update this review"
            );
        }

        // Update the review
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long productId, Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Review not found for product with id: " + productId);
        }

        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You don't have permission to delete this review"
            );
        }

        reviewRepository.delete(review);
    }
}