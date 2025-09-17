package com.ecommerce.dto;

import com.ecommerce.model.Order;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderDtos {

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
        private Long shippingAddressId; // choose from saved addresses for MVP
        @NotNull
        private List<OrderItemRequest> items = new ArrayList<>();

        public Long getShippingAddressId() { return shippingAddressId; }
        public void setShippingAddressId(Long shippingAddressId) { this.shippingAddressId = shippingAddressId; }
        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
    }

    public static class OrderSummary {
        private Long id;
        private BigDecimal total;
        private Order.OrderStatus status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public Order.OrderStatus getStatus() { return status; }
        public void setStatus(Order.OrderStatus status) { this.status = status; }
    }

    public static class UpdateStatusRequest {
        @NotNull
        private Order.OrderStatus status;
        public Order.OrderStatus getStatus() { return status; }
        public void setStatus(Order.OrderStatus status) { this.status = status; }
    }
}
