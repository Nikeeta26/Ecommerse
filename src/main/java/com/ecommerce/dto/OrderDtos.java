package com.ecommerce.dto;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CancelOrderRequest {
        @NotBlank(message = "Cancellation reason is required")
        private String reason;
        private String additionalNotes;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getAdditionalNotes() { return additionalNotes; }
        public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    }

    public static class OrderItemRequest {
        @NotNull
        private Long productId;
        @Min(1)
        private int quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class PlaceOrderRequest {
        @NotNull
        private Long shippingAddressId;
        private List<OrderItemRequest> items = new ArrayList<>();
        private Long cartId; // Optional: if ordering from cart
        private Order.OrderType orderType = Order.OrderType.REGULAR; // Default to REGULAR

        public Long getShippingAddressId() { return shippingAddressId; }
        public void setShippingAddressId(Long shippingAddressId) { this.shippingAddressId = shippingAddressId; }
        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
        public Long getCartId() { return cartId; }
        public void setCartId(Long cartId) { this.cartId = cartId; }
        public Order.OrderType getOrderType() { return orderType; }
        public void setOrderType(Order.OrderType orderType) { this.orderType = orderType; }
    }
    
    public static class BuyNowRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity = 1;
        
        @NotNull(message = "Shipping address ID is required")
        private Long shippingAddressId;
        
        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public Long getShippingAddressId() { return shippingAddressId; }
        public void setShippingAddressId(Long shippingAddressId) { this.shippingAddressId = shippingAddressId; }
    }

    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal subtotal;

        public static OrderItemResponse fromEntity(OrderItem item) {
            OrderItemResponse response = new OrderItemResponse();
            response.setId(item.getId());
            response.setProductId(item.getProduct().getId());
            response.setProductName(item.getProduct().getName());
            response.setProductImage(item.getProduct().getImageUrl());
            response.setUnitPrice(item.getUnitPrice());
            response.setQuantity(item.getQuantity());
            response.setSubtotal(item.getSubtotal());
            return response;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }

    public static class OrderResponse {
        private Long id;
        private String orderNumber;
        private LocalDateTime orderDate;
        private Order.OrderStatus status;
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal shippingCost;
        private BigDecimal total;
        private List<OrderItemResponse> items = new ArrayList<>();
        private Long shippingAddressId;
        private String shippingAddressDetails;

        public static OrderResponse fromEntity(Order order) {
            OrderResponse response = new OrderResponse();
            response.setId(order.getId());
            response.setOrderNumber(order.getOrderNumber());
            response.setOrderDate(order.getOrderDate());
            response.setStatus(order.getStatus());
            response.setSubtotal(order.getSubtotal());
            response.setTax(order.getTax());
            response.setShippingCost(order.getShippingCost());
            response.setTotal(order.getTotal());
            
            if (order.getShippingAddress() != null) {
                response.setShippingAddressId(order.getShippingAddress().getId());
                response.setShippingAddressDetails(order.getShippingAddress().toString());
            }
            
            if (order.getOrderItems() != null) {
                response.setItems(order.getOrderItems().stream()
                    .map(OrderItemResponse::fromEntity)
                    .collect(Collectors.toList()));
            }
            
            return response;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
        public Order.OrderStatus getStatus() { return status; }
        public void setStatus(Order.OrderStatus status) { this.status = status; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public BigDecimal getTax() { return tax; }
        public void setTax(BigDecimal tax) { this.tax = tax; }
        public BigDecimal getShippingCost() { return shippingCost; }
        public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public List<OrderItemResponse> getItems() { return items; }
        public void setItems(List<OrderItemResponse> items) { this.items = items; }
        public Long getShippingAddressId() { return shippingAddressId; }
        public void setShippingAddressId(Long shippingAddressId) { this.shippingAddressId = shippingAddressId; }
        public String getShippingAddressDetails() { return shippingAddressDetails; }
        public void setShippingAddressDetails(String shippingAddressDetails) { this.shippingAddressDetails = shippingAddressDetails; }
    }

    public static class OrderSummary {
        private Long id;
        private String orderNumber;
        private LocalDateTime orderDate;
        private BigDecimal total;
        private Order.OrderStatus status;
        private int totalItems;

        public static OrderSummary fromEntity(Order order) {
            OrderSummary summary = new OrderSummary();
            summary.setId(order.getId());
            summary.setOrderNumber(order.getOrderNumber());
            summary.setOrderDate(order.getOrderDate());
            summary.setTotal(order.getTotal());
            summary.setStatus(order.getStatus());
            summary.setTotalItems(order.getOrderItems() != null ? order.getOrderItems().size() : 0);
            return summary;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public Order.OrderStatus getStatus() { return status; }
        public void setStatus(Order.OrderStatus status) { this.status = status; }
        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
    }

    public static class UpdateStatusRequest {
        @NotNull
        private Order.OrderStatus status;
        public Order.OrderStatus getStatus() { return status; }
        public void setStatus(Order.OrderStatus status) { this.status = status; }
    }
    
    public static class AdminOrderSummary {
        private Long id;
        private String orderNumber;
        private LocalDateTime orderDate;
        private BigDecimal total;
        private Order.OrderStatus status;
        private String customerName;
        private String customerEmail;
        private int totalItems;

        public static AdminOrderSummary fromEntity(Order order) {
            AdminOrderSummary summary = new AdminOrderSummary();
            summary.setId(order.getId());
            summary.setOrderNumber(order.getOrderNumber());
            summary.setOrderDate(order.getOrderDate());
            summary.setTotal(order.getTotal());
            summary.setStatus(order.getStatus());
            
            if (order.getUser() != null) {
                summary.setCustomerName(order.getUser().getName());
                summary.setCustomerEmail(order.getUser().getEmail());
            }
            
            summary.setTotalItems(order.getOrderItems() != null ? order.getOrderItems().size() : 0);
            return summary;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public Order.OrderStatus getStatus() { return status; }
        public void setStatus(Order.OrderStatus status) { this.status = status; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
    }
    
    public static class AdminOrderResponse extends OrderResponse {
        private String customerName;
        private String customerEmail;
        private String customerPhone;
        private LocalDateTime updatedAt;
        private String cancelledReason;
        private LocalDateTime cancelledAt;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;

        public static AdminOrderResponse fromEntity(Order order) {
            AdminOrderResponse response = new AdminOrderResponse();
            
            // Set fields from parent class
            response.setId(order.getId());
            response.setOrderNumber(order.getOrderNumber());
            response.setOrderDate(order.getOrderDate());
            response.setStatus(order.getStatus());
            response.setSubtotal(order.getSubtotal());
            response.setTax(order.getTax());
            response.setShippingCost(order.getShippingCost());
            response.setTotal(order.getTotal());
            
            if (order.getShippingAddress() != null) {
                response.setShippingAddressId(order.getShippingAddress().getId());
                response.setShippingAddressDetails(order.getShippingAddress().toString());
            }
            
            if (order.getOrderItems() != null) {
                response.setItems(order.getOrderItems().stream()
                    .map(OrderItemResponse::fromEntity)
                    .collect(Collectors.toList()));
            }
            
            // Set admin-specific fields
            if (order.getUser() != null) {
                response.setCustomerName(order.getUser().getName());
                response.setCustomerEmail(order.getUser().getEmail());
                response.setCustomerPhone(order.getUser().getPhoneNumber());
            }
            
            response.setUpdatedAt(order.getUpdatedAt());
            response.setCancelledReason(order.getCancelledReason());
            response.setCancelledAt(order.getCancellationDate());
            response.setShippedAt(order.getShippedDate());
            response.setDeliveredAt(order.getDeliveredDate());
            
            return response;
        }

        // Getters and Setters
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        public String getCancelledReason() { return cancelledReason; }
        public void setCancelledReason(String cancelledReason) { this.cancelledReason = cancelledReason; }
        public LocalDateTime getCancelledAt() { return cancelledAt; }
        public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
        public LocalDateTime getShippedAt() { return shippedAt; }
        public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
        public LocalDateTime getDeliveredAt() { return deliveredAt; }
        public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    }
}
