package com.ecommerce.dto;

import com.ecommerce.model.Order;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrderListDTO {
    private Long id;
    private String orderNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;
    
    private String status;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalAmount;
    private int totalItems;

    public static OrderListDTO fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderListDTO dto = new OrderListDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setOrderDate(order.getCreatedAt());
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        
        if (order.getUser() != null) {
            dto.setCustomerName(order.getUser().getFullName());
            dto.setCustomerEmail(order.getUser().getEmail());
        }
        
        dto.setTotalAmount(order.getTotal());
        dto.setTotalItems(order.getOrderItems() != null ? order.getOrderItems().size() : 0);
        
        return dto;
    }
}
