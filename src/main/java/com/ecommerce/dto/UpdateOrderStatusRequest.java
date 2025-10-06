package com.ecommerce.dto;

import com.ecommerce.model.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {
    @NotNull(message = "Status is required")
    private Order.OrderStatus status;
    
    // Optional tracking information
    private String trackingNumber;
    private String carrier;
}
