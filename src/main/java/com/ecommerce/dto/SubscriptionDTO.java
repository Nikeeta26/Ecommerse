package com.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SubscriptionDTO {
    private Long id;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @Valid
    @NotEmpty(message = "At least one product is required")
    private List<SubscriptionProductDTO> products;
    
    @Data
    @NoArgsConstructor
    public static class SubscriptionProductDTO {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        @NotNull(message = "Deposit amount is required for reusable products")
        private BigDecimal depositAmount;
    }
    
    private String deliveryAddress;
    
    // For response
    private String status;
    private List<SubscriptionProductInfoDTO> subscriptionProducts;
    private Integer refillFrequencyDays;
    
    @Data
    @NoArgsConstructor
    public static class SubscriptionProductInfoDTO {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal refillPrice;
        private BigDecimal depositAmount;
    }
}
