package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    public User getUser() {
        return user;
    }
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;
    
    @Column(name = "shipping_cost", precision = 10, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;
    
    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;
    
    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;
    
    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @Column(name = "order_date", nullable = true, updatable = false)
    private LocalDateTime orderDate = LocalDateTime.now();
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "cancelled_reason", length = 1000)
    private String cancelledReason;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum OrderStatus {
        PENDING,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }
    
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
    
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }
    
    // Additional getters
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public String getCancelledReason() {
        return cancelledReason;
    }
    
    // Alias methods for DTO compatibility
    public LocalDateTime getCancelledAt() {
        return cancellationDate;
    }
    
    public LocalDateTime getShippedAt() {
        return shippedDate;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredDate;
    }
    
    // Helper methods for DTO mapping
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal != null ? subtotal : BigDecimal.ZERO;
    }
    
    public BigDecimal getTax() {
        return tax != null ? tax : BigDecimal.ZERO;
    }
    
    public BigDecimal getShippingCost() {
        return shippingCost != null ? shippingCost : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotal() {
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }
    
    // Alias for DTO compatibility
    public List<OrderItem> getItems() {
        return orderItems;
    }
    
    // Status change methods
    public void markAsShipped() {
        this.status = OrderStatus.SHIPPED;
        this.shippedDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsDelivered() {
        this.status = OrderStatus.DELIVERED;
        this.deliveredDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancel(String reason) {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Cannot cancel order with status: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancellationDate = LocalDateTime.now();
        this.cancelledReason = reason;
        this.updatedAt = LocalDateTime.now();
        this.notes = (this.notes != null ? this.notes + "\n" : "") + 
                    "Order cancelled on " + LocalDateTime.now() + 
                    (reason != null ? ": " + reason : "");
    }
    
    // Helper methods
    public boolean canBeCancelled() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.PROCESSING;
    }
    
    public boolean isPaid() {
        // Implement payment status logic if needed
        return this.status != OrderStatus.PENDING;
    }
}
