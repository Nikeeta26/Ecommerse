package com.ecommerce.repository;

import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    
    /**
     * Find all addresses for a given user
     */
    List<Address> findByUser(User user);
    
    /**
     * Find all active addresses for a user, ordered by update time (newest first)
     */
    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.isActive = :isActive ORDER BY a.updatedAt DESC")
    List<Address> findByUserAndIsActiveOrderByUpdatedAtDesc(
        @Param("user") User user, 
        @Param("isActive") boolean isActive
    );
    
    /**
     * Find the most recent active address for a user
     */
    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.isActive = true ORDER BY a.updatedAt DESC")
    Optional<Address> findTopByUserAndIsActiveOrderByUpdatedAtDesc(
        @Param("user") User user
    );
    
    /**
     * Find the default address for a user
     */
    Optional<Address> findByUserAndIsDefault(User user, boolean isDefault);
    
    /**
     * Find all addresses for a user by user ID
     */
    List<Address> findByUserId(Long userId);
    
    /**
     * Find all addresses for a user except the one with the given ID
     */
    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.id != :id")
    List<Address> findByUserAndIdNot(
        @Param("user") User user, 
        @Param("id") Long id
    );
    
    /**
     * Find an address by ID and user
     */
    @Query("SELECT a FROM Address a WHERE a.id = :id AND a.user = :user")
    Optional<Address> findByIdAndUser(
        @Param("id") Long id, 
        @Param("user") User user
    );
    
    /**
     * Deactivate all addresses for a user except the one with the given ID
     */
    @Modifying
    @Query("UPDATE Address a SET a.isActive = false WHERE a.user = :user AND a.id != :id")
    void deactivateOtherAddresses(
        @Param("user") User user, 
        @Param("id") Long id
    );
    
    /**
     * Deactivate all addresses for a user
     */
    @Modifying
    @Query("UPDATE Address a SET a.isActive = false WHERE a.user = :user")
    void deactivateAllUserAddresses(@Param("user") User user);
    
    /**
     * Count the number of addresses for a user
     */
    long countByUser(User user);
}
