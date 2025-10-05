package com.ecommerce.mapper;

import com.ecommerce.dto.ReviewResponse;
import com.ecommerce.model.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    private final UserMapper userMapper;

    public ReviewMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public ReviewResponse toDto(Review review) {
        if (review == null) {
            return null;
        }
        
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setUser(userMapper.toUserDto(review.getUser()));
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }
}
