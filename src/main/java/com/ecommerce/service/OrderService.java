package com.ecommerce.service;

import com.ecommerce.dto.OrderDtos;
import com.ecommerce.exception.InvalidOrderException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    
    Order placeOrder(User user, OrderDtos.PlaceOrderRequest request);
    
    Order getOrderForUser(User user, Long orderId) throws ResourceNotFoundException;
    
    List<Order> getOrdersForUser(User user);
    
    Page<Order> getOrdersForUser(User user, Pageable pageable);
    
    Page<Order> getOrdersForUser(Long userId, Pageable pageable);
    
    List<Order> getAllOrders();
    
    Page<Order> getAllOrders(Pageable pageable);
    
    List<Order> findAll();
    
    Optional<Order> getOrderById(Long orderId);
    
    Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) throws InvalidOrderException;
    
    Order updateOrderTracking(Long orderId, String trackingNumber, String carrier) throws InvalidOrderException;
    
    Order cancelOrder(User user, Long orderId, String reason) throws InvalidOrderException;
    
    /**
     * Find orders for a user with filters
     */
    Page<Order> findUserOrdersWithFilters(
        User user, 
        Order.OrderStatus status, 
        LocalDate fromDate, 
        LocalDate toDate, 
        Pageable pageable);
        
    /**
     * Find all orders with a specific status
     * @param status The status to filter orders by
     * @return List of orders with the specified status
     */
    List<Order> getOrdersByStatus(Order.OrderStatus status);
        
    /**
     * Find all orders with filters (admin only)
     */
    Page<Order> findAllWithFilters(
        Long userId,
        Order.OrderStatus status,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable);
        
    /**
     * Get all orders with a specific status
     * @param status The status to filter orders by
     * @return List of orders with the specified status
     */

}
