package com.ecommerce.repository;

import com.ecommerce.model.Subscription;
import com.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    List<Subscription> findByUserIdAndActiveTrue(Long userId);

    Optional<Subscription> findByIdAndUserId(Long id, Long userId);
    
//    Optional<Subscription> findByUserIdAndActiveTrue(Long userId);
    
    List<Subscription> findByNextRefillDateBeforeAndActiveTrue(LocalDateTime dateTime);
    
    boolean existsByUserIdAndActiveTrue(Long userId);
    
    @Query("SELECT COUNT(s) > 0 FROM Subscription s JOIN s.products p WHERE s.user.id = :userId AND p.id = :productId AND s.active = true")
    boolean existsByUserIdAndProductIdAndActiveTrue(@Param("userId") Long userId, @Param("productId") Long productId);
    
    @Query("SELECT s FROM Subscription s JOIN s.products p WHERE s.user.id = :userId AND p.id = :productId AND s.active = true")
    Optional<Subscription> findByUserIdAndProductIdAndActiveTrue(@Param("userId") Long userId, @Param("productId") Long productId);
}
