package com.ecommerce.controller;

import com.ecommerce.dto.OrderDtos;
import com.ecommerce.model.Order;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.OrderService;
import com.ecommerce.dto.OrderListDTO;
import com.ecommerce.model.Order;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.model.Order;
import com.ecommerce.exception.InvalidOrderException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.dto.UpdateOrderStatusRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Collections;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
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
     * Cancel an order
     */
    /**
     * Cancel an order
     * @param principal The authenticated user
     * @param orderId The ID of the order to cancel
     * @param request The cancellation request containing reason and optional notes
     * @return The updated order details
     */
    @PostMapping("/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId,
            @RequestBody(required = false) String requestBody) {
        
        logger.info("Received cancel order request for order {}", orderId);
        
        try {
            // Parse the request body manually to debug
            logger.debug("Raw request body: {}", requestBody);
            
            // Default values
            String reason = "Order cancelled by user";
            String additionalNotes = "";
            
            if (requestBody != null && !requestBody.trim().isEmpty()) {
                try {
                    // Remove all whitespace and braces for simpler parsing
                    String cleanBody = requestBody.replaceAll("\\s", "");
                    
                    // Extract reason
                    int reasonStart = cleanBody.indexOf("\"reason\":\"");
                    if (reasonStart > 0) {
                        reason = cleanBody.substring(reasonStart + 10, cleanBody.indexOf("\"", reasonStart + 10));
                    }
                    
                    // Extract additionalNotes if present
                    int notesStart = cleanBody.indexOf("\"additionalNotes\":\"");
                    if (notesStart > 0) {
                        additionalNotes = cleanBody.substring(notesStart + 18, cleanBody.indexOf("\"", notesStart + 18));
                    }
                } catch (Exception e) {
                    logger.warn("Error parsing request body: {}", e.getMessage());
                    // Continue with default values
                }
            }
            
            logger.info("Parsed cancel request - reason: {}, additionalNotes: {}", reason, additionalNotes);
            
            var user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            logger.debug("Found user: {}", user.getId());
            
            // Call the service with the parsed reason
            Order order = orderService.cancelOrder(user, orderId, reason);
            
            // Log additional notes if provided
            if (additionalNotes != null && !additionalNotes.isEmpty()) {
                logger.info("Additional notes for order {} cancellation: {}", orderId, additionalNotes);
            }
            
            logger.info("Order {} cancelled successfully by user {}", orderId, user.getId());
            
            return ResponseEntity.ok(OrderDtos.OrderResponse.fromEntity(order));
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Order not found: {}", orderId);
            return ResponseEntity.status(404)
                .body(Collections.singletonMap("error", "Order not found"));
                
        } catch (InvalidOrderException e) {
            logger.warn("Invalid order cancellation request for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(Collections.singletonMap("error", e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error cancelling order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "An error occurred while processing your request"));
        }
    }
    /**
     * Get current user's order history with pagination and filtering
     */
    /**
     * Get all orders (public endpoint)
     * @return List of simplified order information
     */
    @GetMapping("/all")
    public ResponseEntity<List<OrderListDTO>> getAllOrders() {
        List<OrderListDTO> orders = orderService.findAll().stream()
                .map(OrderListDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }
    
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

    // Admin endpoints
     /* Get all orders with filtering and pagination (admin only)
     */
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/all")
//    public ResponseEntity<List<Order>> getAllOrders() {
//        return ResponseEntity.ok(orderService.findAll());
//    }
    

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
     * Admin: Get order details
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders/{orderId}")
    public ResponseEntity<OrderDtos.AdminOrderResponse> getOrderDetailsAdmin(
            @PathVariable Long orderId) {

        Order order = orderService.getOrderById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        return ResponseEntity.ok(OrderDtos.AdminOrderResponse.fromEntity(order));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<OrderDtos.OrderStatusResponse>> getOrdersByStatus(
            @PathVariable("status") String statusStr) {
            
        try {
            // Convert the string status to enum
            Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
            
            logger.info("Fetching orders with status: {}", status);
            List<Order> orders = orderService.getOrdersByStatus(status);
            
            List<OrderDtos.OrderStatusResponse> response = orders.stream()
                .map(OrderDtos.OrderStatusResponse::fromEntity)
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid status value: {}", statusStr, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error fetching orders by status: {}", statusStr, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update order status
     * @param orderId ID of the order to update
     * @param request UpdateOrderStatusRequest containing the new status and optional tracking info
     * @return ResponseEntity with the updated order status or error message
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        
        logger.info("Updating status for order {} to {}", orderId, request.getStatus());
        
        try {
            // Update the order status
            Order updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus());
            logger.info("Successfully updated order {} status to {}", orderId, updatedOrder.getStatus());
            
            // If tracking info is provided, update it
            if (request.getTrackingNumber() != null || request.getCarrier() != null) {
                logger.debug("Updating tracking info for order {}: {} - {}", 
                    orderId, request.getCarrier(), request.getTrackingNumber());
                    
                updatedOrder = orderService.updateOrderTracking(
                    orderId, 
                    request.getTrackingNumber(), 
                    request.getCarrier()
                );
                logger.info("Successfully updated tracking info for order {}", orderId);
            }
            
            // Return the simplified response with 200 OK
            return ResponseEntity.ok(OrderDtos.OrderStatusResponse.fromEntity(updatedOrder));
            
        } catch (InvalidOrderException e) {
            // Create a response with error status
            Order errorOrder = new Order();
            errorOrder.setId(orderId);
            errorOrder.setStatus(Order.OrderStatus.PENDING); // Default status
            errorOrder.setUpdatedAt(LocalDateTime.now());
            OrderDtos.OrderStatusResponse errorResponse = OrderDtos.OrderStatusResponse.fromEntity(errorOrder);
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            // Create a response with error status
            Order errorOrder = new Order();
            errorOrder.setId(orderId);
            errorOrder.setStatus(Order.OrderStatus.PENDING); // Default status
            errorOrder.setUpdatedAt(LocalDateTime.now());
            OrderDtos.OrderStatusResponse errorResponse = OrderDtos.OrderStatusResponse.fromEntity(errorOrder);
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
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







//
//rder and Cart API Documentation
//Cart Endpoints
//1. Get Active Cart
//URL: GET /api/carts
//Authentication: Required (JWT)
//Response:
//json
//{
//    "id": 1,
//        "items": [
//    {
//        "id": 1,
//            "productId": 101,
//            "productName": "Product Name",
//            "unitPrice": 29.99,
//            "quantity": 2,
//            "subtotal": 59.98
//    }
//  ],
//    "total": 59.98,
//        "totalItems": 1
//}
//2. Add Item to Cart
//URL: POST /api/carts/items
//Authentication: Required (JWT)
//Request:
//json
//{
//    "productId": 101,
//        "quantity": 1
//}
//Response: Same as Get Active Cart
//3. Update Cart Item Quantity
//URL: PUT /api/carts/items/{itemId}
//Authentication: Required (JWT)
//Request:
//json
//{
//    "productId": 101,
//        "quantity": 3
//}
//Response: Same as Get Active Cart
//4. Remove Item from Cart
//URL: DELETE /api/carts/items/{itemId}
//Authentication: Required (JWT)
//Response: Same as Get Active Cart
//5. Clear Cart
//URL: DELETE /api/carts
//Authentication: Required (JWT)
//Response: 204 No Content
//Order Endpoints
//1. Place Order
//URL: POST /api/orders
//Authentication: Required (JWT)
//Request:
//json
//{
//    "shippingAddressId": 1,
//        "items": [
//    {
//        "productId": 101,
//            "quantity": 2
//    }
//  ],
//    "cartId": 1
//}
//Response:
//json
//{
//    "id": 1,
//        "orderNumber": "ORD-12345",
//        "orderDate": "2025-09-30T12:00:00",
//        "status": "PENDING",
//        "subtotal": 59.98,
//        "tax": 5.99,
//        "shippingCost": 4.99,
//        "total": 70.96,
//        "items": [
//    {
//        "id": 1,
//            "productId": 101,
//            "productName": "Product Name",
//            "productImage": "image-url.jpg",
//            "unitPrice": 29.99,
//            "quantity": 2,
//            "subtotal": 59.98
//    }
//  ],
//    "shippingAddressId": 1,
//        "shippingAddressDetails": "123 Main St, City, Country"
//}
//2. Get Order Details
//URL: GET /api/orders/{orderId}
//Authentication: Required (JWT)
//Response: Same as Place Order response
//3. Get User's Orders
//URL: GET /api/orders/my-orders?page=0&size=10&status=DELIVERED&fromDate=2025-01-01&toDate=2025-12-31
//Query Parameters:
//page: Page number (default: 0)
//size: Items per page (default: 10)
//status: Filter by status (optional)
//fromDate: Filter orders after this date (optional, format: yyyy-MM-dd)
//toDate: Filter orders before this date (optional, format: yyyy-MM-dd)
//Response:
//json
//{
//    "content": [
//    {
//        "id": 1,
//            "orderNumber": "ORD-12345",
//            "orderDate": "2025-09-30T12:00:00",
//            "status": "DELIVERED",
//            "total": 70.96,
//            "totalItems": 2
//    }
//  ],
//    "pageable": { ... },
//    "totalPages": 1,
//        "totalElements": 1,
//        "last": true,
//        "size": 10,
//        "number": 0,
//        "sort": { ... },
//    "first": true,
//        "numberOfElements": 1,
//        "empty": false
//}
//4. Cancel Order
//URL: POST /api/orders/{orderId}/cancel?reason=Changed%20mind
//Authentication: Required (JWT)
//Query Parameters:
//reason: Reason for cancellation (optional)
//Response: Same as Get Order Details
//5. Admin: Get All Orders (Admin Only)
//URL: GET /api/orders/admin?page=0&size=20&userId=1&status=DELIVERED&fromDate=2025-01-01&toDate=2025-12-31
//Authentication: Required (JWT with ADMIN role)
//Query Parameters: Same as user's orders, plus:
//userId: Filter by user ID (optional)
//Response: Paginated list of orders with admin details
//6. Admin: Get Orders for User (Admin Only)
//URL: GET /api/orders/user/{userId}?page=0&size=10
//Authentication: Required (JWT with ADMIN role)
//Response: Paginated list of user's orders with admin details
//        7. Admin: Update Order Status (Admin Only)
//URL: PUT /api/orders/{orderId}/status
//Authentication: Required (JWT with ADMIN role)
//Request:
//json
//{
//    "status": "SHIPPED"
//}
//Response: Updated order details with admin fields
//8. Admin: Get Order Details (Admin Only)
//URL: GET /api/orders/admin/{orderId}
//Authentication: Required (JWT with ADMIN role)
//Response: Order details with admin fields
//        Enums
//OrderStatus
//        PENDING
//PROCESSING
//        SHIPPED
//DELIVERED
//        CANCELLED
//REFUNDED
//        Notes
//All endpoints require authentication via JWT token in the Authorization header
//




//GET http://localhost:8080/api/orders/all