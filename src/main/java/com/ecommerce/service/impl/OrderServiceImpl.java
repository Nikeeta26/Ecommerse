package com.ecommerce.service.impl;

import com.ecommerce.dto.OrderDtos;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InvalidOrderException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.model.User.UserRole;
import com.ecommerce.repository.*;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    
    public OrderServiceImpl(OrderRepository orderRepository,
                          ProductRepository productRepository,
                          OrderItemRepository orderItemRepository,
                          CartService cartService,
                          AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional
    public Order placeOrder(User user, OrderDtos.PlaceOrderRequest request) {
        logger.info("Placing order for user: {}", user.getId());
        
        // Validate request
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        
        // Create new order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        
        try {
            // Set shipping address
            Address shippingAddress = addressRepository.findByIdAndUser(request.getShippingAddressId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found or does not belong to user"));
            order.setShippingAddress(shippingAddress);
            
            // Process order items
            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal subtotal = BigDecimal.ZERO;
            
            for (OrderDtos.OrderItemRequest itemRequest : request.getItems()) {
                if (itemRequest.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be greater than 0");
                }
                
                Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));
                    
                if (product.getStock() < itemRequest.getQuantity()) {
                    throw new InsufficientStockException(
                        String.format("Insufficient stock for product: %s. Available: %d, Requested: %d", 
                            product.getName(), product.getStock(), itemRequest.getQuantity()));
                }
                
                // Create order item
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setUnitPrice(product.getPrice());
                
                // Calculate subtotal (will be set by @PrePersist)
                orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
                
                // Add to order
                order.addOrderItem(orderItem);
                orderItems.add(orderItem);
                
                // Update running total
                subtotal = subtotal.add(orderItem.getSubtotal());
                
                // Update product stock
                product.setStock(product.getStock() - itemRequest.getQuantity());
                productRepository.save(product);
                
                logger.debug("Added product {} (qty: {}) to order", product.getName(), itemRequest.getQuantity());
            }
            
            // Calculate totals
            BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1)); // 10% tax
            BigDecimal shippingCost = calculateShippingCost(order);
            BigDecimal total = subtotal.add(tax).add(shippingCost);
            
            // Set order amounts
            order.setSubtotal(subtotal);
            order.setTax(tax);
            order.setShippingCost(shippingCost);
            order.setTotalAmount(total);
            
            // Save order first to get an ID
            Order savedOrder = orderRepository.save(order);
            
            // Save order items with the saved order reference
            for (OrderItem item : orderItems) {
                item.setOrder(savedOrder);
                orderItemRepository.save(item);
            }
            
            // Clear the user's cart after successful order placement
            try {
                cartService.clearUserCart(user);
                logger.debug("Cleared cart after order placement for user {}", user.getId());
            } catch (Exception e) {
                // Log error but don't fail the order if cart clearing fails
                logger.error("Failed to clear cart after order placement for user {}: {}", 
                    user.getId(), e.getMessage());
            }
            
            logger.info("Order placed successfully. Order ID: {}, Order Number: {}", 
                savedOrder.getId(), savedOrder.getOrderNumber());
                
            return savedOrder;
            
        } catch (Exception e) {
            logger.error("Error placing order: {}", e.getMessage(), e);
            throw e; // Re-throw to be handled by the controller
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Order getOrderForUser(User user, Long orderId) {
        logger.debug("Fetching order {} for user {}", orderId, user.getId());
        return orderRepository.findByIdAndUser(orderId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(User user) {
        logger.debug("Fetching all orders for user {}", user.getId());
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersForUser(User user, Pageable pageable) {
        logger.debug("Fetching paginated orders for user {}", user.getId());
        return orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersForUser(Long userId, Pageable pageable) {
        logger.debug("Fetching paginated orders for user ID: {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        logger.debug("Fetching all orders");
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        logger.debug("Fetching all orders (unfiltered)");
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(
            PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
            )
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long orderId) {
        logger.debug("Fetching order by ID: {}", orderId);
        return orderRepository.findById(orderId);
    }
    
    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) throws InvalidOrderException {
        logger.info("Updating order {} status to {}", orderId, newStatus);
        
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
            
        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);
        
        // Update status and relevant timestamps
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Handle status-specific logic
        switch (newStatus) {
            case PENDING:
                logger.info("Order {} is now pending", orderId);
                break;
                
            case PROCESSING:
                logger.info("Order {} is now being processed", orderId);
                break;
                
            case SHIPPED:
                order.setShippedDate(LocalDateTime.now());
                logger.info("Order {} has been shipped", orderId);
                break;
                
            case DELIVERED:
                order.setDeliveredDate(LocalDateTime.now());
                logger.info("Order {} has been delivered", orderId);
                break;
                
            case CANCELLED:
                // Restore product stock when cancelling
                restoreStockForOrder(order);
                order.cancel("Status updated to CANCELLED");
                logger.info("Order {} has been cancelled", orderId);
                break;
                
            case REFUNDED:
                // Handle refund logic if needed
                order.setUpdatedAt(LocalDateTime.now());
                logger.info("Order {} has been refunded", orderId);
                break;
        }
        
        // Save the updated order
        Order updatedOrder = orderRepository.save(order);
        logger.debug("Order {} status updated from {} to {}", orderId, oldStatus, newStatus);
        
        return updatedOrder;
    }
    
    @Override
    @Transactional
    public Order updateOrderTracking(Long orderId, String trackingNumber, String carrier) throws InvalidOrderException {
        logger.info("Updating tracking info for order {}: {}/{}", orderId, carrier, trackingNumber);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new InvalidOrderException("Order not found with id: " + orderId));
            
        // Update tracking info if provided
        if (trackingNumber != null) {
            order.setTrackingNumber(trackingNumber);
        }
        
//        if (carrier != null) {
//            order.setShippingCarrier(carrier);
//        }
        
        // If we have both tracking number and carrier, and status is not already SHIPPED,
        // automatically update status to SHIPPED
        if (trackingNumber != null && carrier != null && 
            order.getStatus() != Order.OrderStatus.SHIPPED) {
            order.setStatus(Order.OrderStatus.SHIPPED);
            order.setShippedDate(LocalDateTime.now());
            logger.info("Order {} status updated to SHIPPED with tracking info", orderId);
        }
        
        return orderRepository.save(order);
    }
    
    @Override
    @Transactional
    public Order cancelOrder(User user, Long orderId, String reason) throws InvalidOrderException {
        logger.info("User {} is cancelling order {}", user.getId(), orderId);
        
        // Find the order
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
            
        // Verify user has permission to cancel this order
        if (!order.getUser().getId().equals(user.getId()) && !user.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new InvalidOrderException("You are not authorized to cancel this order");
        }
        
        // Check if order can be cancelled
        if (!order.canBeCancelled()) {
            throw new InvalidOrderException("Order cannot be cancelled in its current state: " + order.getStatus());
        }
        
        // Restore product stock
        restoreStockForOrder(order);
        
        // Update order status and cancellation details using the cancel method
        order.cancel(reason);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Save the updated order
        Order cancelledOrder = orderRepository.save(order);
        logger.info("Order {} has been cancelled by user {}", orderId, user.getId());
        
        return cancelledOrder;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Order> findUserOrdersWithFilters(
            User user, 
            Order.OrderStatus status, 
            LocalDate fromDate, 
            LocalDate toDate, 
            Pageable pageable) {
        
        logger.debug("Finding orders for user {} with filters - status: {}, from: {}, to: {}", 
            user.getId(), status, fromDate, toDate);
            
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date must be before or equal to To date");
        }
        
        return orderRepository.findByUserAndFilters(
            user, 
            status, 
            fromDate != null ? fromDate.atStartOfDay() : null,
            toDate != null ? toDate.plusDays(1).atStartOfDay() : null,
            pageable
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Order> findAllWithFilters(
            Long userId,
            Order.OrderStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {
        
        logger.debug("Finding all orders with filters - userId: {}, status: {}, from: {}, to: {}", 
            userId, status, fromDate, toDate);
            
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date must be before or equal to To date");
        }
        
        return orderRepository.findAllWithFilters(
            userId,
            status,
            fromDate != null ? fromDate.atStartOfDay() : null,
            toDate != null ? toDate.plusDays(1).atStartOfDay() : null,
            pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        logger.debug("Fetching orders with status: {}", status);
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Calculate shipping cost based on order details
     */
    private BigDecimal calculateShippingCost(Order order) {
        // Simple flat rate shipping for now
        // In a real application, this could be based on weight, destination, etc.
        return BigDecimal.valueOf(10.00);
    }
    
    /**
     * Restore product stock when an order is cancelled
     */
    private void restoreStockForOrder(Order order) {
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                    logger.debug("Restored stock for product {}. New stock: {}",
                            product.getName(), product.getStock());
                }
            }
        }
    }
    
    /**
     * Validates if a status transition is allowed
     * @param oldStatus The current order status
     * @param newStatus The new order status to transition to
     * @throws InvalidOrderException if the transition is not allowed
     */
    private void validateStatusTransition(Order.OrderStatus oldStatus, Order.OrderStatus newStatus) 
            throws InvalidOrderException {
        logger.debug("Validating status transition from {} to {}", oldStatus, newStatus);
        
        // Allow same status (no-op)
        if (oldStatus == newStatus) {
            return;
        }
        
        boolean isValidTransition = false;
        
        switch (oldStatus) {
            case PENDING:
                // PENDING can transition to PROCESSING or CANCELLED
                isValidTransition = (newStatus == Order.OrderStatus.PROCESSING || 
                                   newStatus == Order.OrderStatus.CANCELLED);
                break;
                
            case PROCESSING:
                // PROCESSING can transition to SHIPPED or CANCELLED
                isValidTransition = (newStatus == Order.OrderStatus.SHIPPED || 
                                   newStatus == Order.OrderStatus.CANCELLED);
                break;
                
            case SHIPPED:
                // SHIPPED can only transition to DELIVERED
                isValidTransition = (newStatus == Order.OrderStatus.DELIVERED);
                break;
                
            case DELIVERED:
                // DELIVERED can only transition to REFUNDED
                isValidTransition = (newStatus == Order.OrderStatus.REFUNDED);
                break;
                
            case CANCELLED:
            case REFUNDED:
                // No further status changes allowed from these states
                isValidTransition = false;
                break;
                
            default:
                throw new InvalidOrderException("Unknown order status: " + oldStatus);
        }
        
        if (!isValidTransition) {
            throw new InvalidOrderException(
                String.format("Invalid status transition from %s to %s", oldStatus, newStatus)
            );
        }
    }
}
