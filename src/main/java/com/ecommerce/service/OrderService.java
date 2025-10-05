package com.ecommerce.service;

import com.ecommerce.dto.DirectOrderRequest;
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
    
    
    List<Order> getOrdersForUser(User user);
    
    Page<Order> getOrdersForUser(User user, Pageable pageable);
    
    Order getOrderForUser(User user, Long orderId);
    
    /**
     * Get order by ID without user context (admin only)
     * @param orderId The ID of the order to retrieve
     * @return The order with the specified ID
     * @throws ResourceNotFoundException if the order is not found
     */
    Order getOrderById(Long orderId);
    
    Page<Order> searchOrders(String query, Pageable pageable);
    
    /**
     * Place a direct order without adding to cart
     * @param user The user placing the order
     * @param request The direct order request
     * @return The created order
     */
    Order placeDirectOrder(User user, DirectOrderRequest request);
    
    List<Order> getAllOrders();
    
    Page<Order> getAllOrders(Pageable pageable);
    

    Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) throws InvalidOrderException;
    
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
     * Find all orders with filters (admin only)
     */
    Page<Order> findAllWithFilters(
        Long userId,
        Order.OrderStatus status,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable);
        
    /**
     * Find all orders with filters (admin only)
     */
    Page<Order> findAllWithFilters(
        Order.OrderStatus status,
        LocalDate fromDate,
        LocalDate toDate,
        String searchQuery,
        Pageable pageable);
        
    /**
     * Creates a refill order for a subscription
     * @param subscription The subscription to create the refill order for
     * @return The created order
     * @throws InvalidOrderException If the refill order cannot be created
     */
    Order createRefillOrder(com.ecommerce.model.Subscription subscription) throws InvalidOrderException;
    
    /**
     * Finds all refill orders for a subscription
     * @param subscriptionId The ID of the subscription
     * @return List of refill orders
     */
    List<Order> findRefillOrdersBySubscription(Long subscriptionId);
}
