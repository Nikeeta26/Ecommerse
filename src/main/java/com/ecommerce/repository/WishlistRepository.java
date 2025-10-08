package com.ecommerce.repository;

import com.ecommerce.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    @Query("SELECT w FROM Wishlist w WHERE w.user.email = :email AND w.active = true")
    List<Wishlist> findAllActiveByUserEmail(@Param("email") String email);
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.email = :email AND w.product.id = :productId AND w.active = true")
    Optional<Wishlist> findActiveByUserEmailAndProductId(
        @Param("email") String email, 
        @Param("productId") Long productId
    );

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Wishlist w WHERE w.user.email = :email AND w.product.id = :productId AND w.active = true")
    boolean existsActiveByUserEmailAndProductId(
        @Param("email") String email,
        @Param("productId") Long productId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Wishlist w SET w.active = false WHERE w.user.email = :email AND w.product.id = :productId AND w.active = true")
    int softDeleteByUserEmailAndProductId(
        @Param("email") String email,
        @Param("productId") Long productId
    );
    
    @Query("SELECT w FROM Wishlist w WHERE w.user.email = :email AND w.product.id = :productId")
    Optional<Wishlist> findByUserEmailAndProductId(
        @Param("email") String email, 
        @Param("productId") Long productId
    );
    
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.user.email = :email AND w.active = true")
    int countByUserEmail(@Param("email") String email);
    
    @Query("SELECT w FROM Wishlist w WHERE w.id = :id AND w.user.email = :email AND w.active = true")
    Optional<Wishlist> findByIdAndUserEmail(@Param("id") Long id, @Param("email") String email);
}
