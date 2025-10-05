package com.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RefillRequestDTO {
    @NotNull(message = "Subscription ID is required")
    private Long subscriptionId;
    
    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;
    
    @NotNull(message = "At least one product is required for refill")
    private List<RefillItemDTO> items;
    
    @Data
    public static class RefillItemDTO {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}
