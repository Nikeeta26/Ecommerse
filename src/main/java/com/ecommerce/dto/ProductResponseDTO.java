package com.ecommerce.dto;

import com.ecommerce.model.Product;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProductResponseDTO {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Product.Category category;
    private Boolean active;
    
    // Reusable product fields
    private Boolean reusable;
    private Boolean requiresSubscription;
    private BigDecimal depositAmount;
    private BigDecimal refillPrice;
    private Integer refillQuantity;
    private Integer refillFrequencyDays;

    public static ProductResponseDTO fromProduct(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategory(product.getCategory());
        dto.setReusable(product.isReusable());
        dto.setRequiresSubscription(product.isRequiresSubscription());
        dto.setDepositAmount(product.getDepositAmount());
        dto.setRefillPrice(product.getRefillPrice());
        dto.setRefillQuantity(product.getRefillQuantity());
        dto.setRefillFrequencyDays(product.getRefillFrequencyDays());

        
        return dto;
    }
}
