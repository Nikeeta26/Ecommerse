package com.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SubscriptionRequestDTO {
    
    @Valid
    @NotNull(message = "At least one product is required")
    @Size(min = 1, message = "At least one product is required")
    private List<SubscriptionProductDTO> products;
    
    // Optional name for the subscription (e.g., "Monthly Essentials")
    private String subscriptionName;
    
    // Payment details would be handled by your payment service
    private String paymentMethodId;
    
    @Data
    public static class SubscriptionProductDTO {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        
        // Add any product-specific subscription settings here if needed
    }
}
