package com.ecommerce.dto;

import com.ecommerce.dto.OrderDtos.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class DirectOrderRequest {
    
    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;
    
    @Valid
    @NotNull(message = "At least one order item is required")
    @Size(min = 1, message = "At least one order item is required")
    private List<OrderItemRequest> items;
    
    private String notes;
}
