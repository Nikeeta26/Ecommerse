package com.ecommerce.dto;

import com.ecommerce.model.CartItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class CartDtos {
    public static class AddItemRequest {
        @NotNull
        private Long productId;
        @Min(1)
        private int quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class UpdateQtyRequest {
        @NotNull
        private Long productId;
        @Min(1)
        private int quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class CartItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal subtotal;

        public static CartItemResponse fromEntity(CartItem item) {
            CartItemResponse response = new CartItemResponse();
            response.setId(item.getId());
            response.setProductId(item.getProduct().getId());
            response.setProductName(item.getProduct().getName());
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
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }

    public static class CartResponse {
        private Long id;
        private List<CartItemResponse> items;
        private BigDecimal total;
        private int totalItems;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public List<CartItemResponse> getItems() { return items; }
        public void setItems(List<CartItemResponse> items) { this.items = items; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
    }
}
