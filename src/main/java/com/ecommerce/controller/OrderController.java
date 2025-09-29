package com.ecommerce.controller;

import com.ecommerce.dto.OrderDtos;
import com.ecommerce.model.Order;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.OrderService;
import com.ecommerce.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @Autowired
    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    /**
     * Place a new order
     */
    @PostMapping
    public ResponseEntity<OrderDtos.OrderResponse> placeOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OrderDtos.PlaceOrderRequest request) {
        
        var user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = orderService.placeOrder(user, request);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(order.getId()).toUri();
        
        return ResponseEntity.created(location).body(OrderDtos.OrderResponse.fromEntity(order));
    }

    /**
     * Get order details by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDtos.OrderResponse> getOrderDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId) {
        
        var user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = orderService.getOrderForUser(user, orderId);
        return ResponseEntity.ok(OrderDtos.OrderResponse.fromEntity(order));
    }

    /**
     * Get current user's order history with pagination and filtering
     */
    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderDtos.OrderSummary>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        var user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // If no filters are applied, return all orders
        if (status == null && fromDate == null && toDate == null) {
            return ResponseEntity.ok(orderService.getOrdersForUser(user, pageable)
                .map(OrderDtos.OrderSummary::fromEntity));
        }
        
        // Apply filters
        Page<Order> orders = orderService.findUserOrdersWithFilters(
            user, status, fromDate, toDate, pageable);
            
        return ResponseEntity.ok(orders.map(OrderDtos.OrderSummary::fromEntity));
    }

    /**
     * Cancel an order (if allowed)
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDtos.OrderResponse> cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        
        var user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order cancelledOrder = orderService.cancelOrder(user, orderId, reason);
        return ResponseEntity.ok(OrderDtos.OrderResponse.fromEntity(cancelledOrder));
    }

    // Admin endpoints
    
    /**
     * Get all orders with filtering and pagination (admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<Page<OrderDtos.AdminOrderSummary>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Order> orders = orderService.findAllWithFilters(
            userId, status, fromDate, toDate, pageable);
            
        return ResponseEntity.ok(orders.map(OrderDtos.AdminOrderSummary::fromEntity));
    }
    
    /**
     * Get orders for a specific user (admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderDtos.AdminOrderSummary>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderService.getOrdersForUser(userId, pageable);
        
        return ResponseEntity.ok(orders.map(OrderDtos.AdminOrderSummary::fromEntity));
    }

    /**
     * Update order status (admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDtos.AdminOrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderDtos.UpdateStatusRequest request) {
            
        Order updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus());
        
        // The OrderService implementation should handle date updates based on status
        // as it's part of the business logic
        
        return ResponseEntity.ok(OrderDtos.AdminOrderResponse.fromEntity(updatedOrder));
    }
    
    /**
     * Get order details (admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{orderId}")
    public ResponseEntity<OrderDtos.AdminOrderResponse> getOrderDetailsAdmin(
            @PathVariable Long orderId) {
                
        Order order = orderService.getOrderById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
            
        return ResponseEntity.ok(OrderDtos.AdminOrderResponse.fromEntity(order));
    }
}





//ALTER TABLE admins ALTER COLUMN password SET NOT NULL;
//
//ALTER TABLE orders ADD COLUMN order_number VARCHAR(255);
//
//UPDATE orders SET order_number = 'ORD-' || id WHERE order_number IS NULL;
//
//ALTER TABLE orders ALTER COLUMN order_number SET NOT NULL;

//ALTER TABLE orders ADD COLUMN order_date TIMESTAMP;
