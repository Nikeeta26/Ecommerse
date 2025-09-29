package com.ecommerce.repository;

import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find all orders for a specific user
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    // Find paginated orders for a specific user
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find paginated orders for a specific user by user ID
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Find all orders with pagination
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Find order by ID and user
    Optional<Order> findByIdAndUser(Long id, User user);
    
    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Find orders by status
    List<Order> findByStatus(Order.OrderStatus status);
    
    // Find orders by status with pagination
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    // Find orders by user and status
    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, Order.OrderStatus status);
    
    // Find orders by user and status with pagination
    Page<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, Order.OrderStatus status, Pageable pageable);
    
    // Custom query to find recent orders
    @Query("SELECT o FROM Order o WHERE o.user = :user ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUser(@Param("user") User user, Pageable pageable);
    
    // Check if order exists for user
    boolean existsByIdAndUser(Long id, User user);
    
    /**
     * Find orders for a user with filters
     * @param user The user to find orders for
     * @param status Optional status filter
     * @param fromDate Optional start date filter (inclusive)
     * @param toDate Optional end date filter (inclusive)
     * @param pageable Pagination information
     * @return Page of orders matching the criteria
     */
    @Query("""
        SELECT o FROM Order o 
        WHERE o.user = :user 
        AND (:status IS NULL OR o.status = :status)
        AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
        AND (:toDate IS NULL OR o.createdAt <= :toDate)
        ORDER BY o.createdAt DESC
    """)
    Page<Order> findByUserAndFilters(
        @Param("user") User user,
        @Param("status") Order.OrderStatus status,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );
    
    /**
     * Find all orders with filters (admin only)
     * @param userId Optional user ID filter
     * @param status Optional status filter
     * @param fromDate Optional start date filter (inclusive)
     * @param toDate Optional end date filter (inclusive)
     * @param pageable Pagination information
     * @return Page of orders matching the criteria
     */
    @Query("""
        SELECT o FROM Order o 
        WHERE (:userId IS NULL OR o.user.id = :userId)
        AND (:status IS NULL OR o.status = :status)
        AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
        AND (:toDate IS NULL OR o.createdAt <= :toDate)
        ORDER BY o.createdAt DESC
    """)
    Page<Order> findAllWithFilters(
        @Param("userId") Long userId,
        @Param("status") Order.OrderStatus status,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );
}
