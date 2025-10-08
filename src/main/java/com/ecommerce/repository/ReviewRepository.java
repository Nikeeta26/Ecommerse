package com.ecommerce.repository;

import com.ecommerce.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByProductId(Long productId);
    
    @Query("SELECT r FROM Review r WHERE r.user.email = :email")
    List<Review> findByUserEmail(@Param("email") String email);
    
    @Query("SELECT r FROM Review r WHERE r.id = :reviewId AND r.user.email = :email")
    Optional<Review> findByIdAndUserEmail(@Param("reviewId") Long reviewId, @Param("email") String email);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.user.email = :email AND r.product.id = :productId")
    boolean existsByUserEmailAndProductId(@Param("email") String email, @Param("productId") Long productId);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.id = :reviewId AND r.user.email = :email")
    boolean existsByIdAndUserEmail(@Param("reviewId") Long reviewId, @Param("email") String email);
    
    // Keeping these for backward compatibility
    @Deprecated
    List<Review> findByUserId(Long userId);
    
    @Deprecated
    Optional<Review> findByIdAndUserId(Long reviewId, Long userId);
    
    @Deprecated
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    @Deprecated
    boolean existsByIdAndUserId(Long reviewId, Long userId);
}
