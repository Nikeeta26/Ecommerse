package com.ecommerce.model;

import com.ecommerce.config.serializer.ProductKeyDeserializer;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


import com.fasterxml.jackson.annotation.JsonManagedReference;

import com.ecommerce.model.Review;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer stock = 0;
    
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Review> reviews = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType = ProductType.REGULAR;
    
    // Reusable product specific fields
    @Column(name = "is_reusable", nullable = false, columnDefinition = "boolean default false")
    private boolean reusable = false;
    
    @Column(name = "requires_subscription", nullable = false, columnDefinition = "boolean default false")
    private boolean requiresSubscription = false;
    
    @Column(name = "deposit_amount")
    private BigDecimal depositAmount;
    
    @Column(name = "refill_price")
    private BigDecimal refillPrice;
    
    @Column(name = "refill_quantity")
    private Integer refillQuantity;
    
    @Column(name = "refill_frequency_days")
    private Integer refillFrequencyDays;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OrderItem> orderItems;
    
    @ManyToMany(mappedBy = "products")
    @JsonIgnore
    private Set<Subscription> subscriptions = new HashSet<>();
    
    public enum Category {
        ELECTRONICS,
        CLOTHING,
        BOOKS,
        HOME_APPLIANCES,
        SPORTS,
        BEAUTY,
        TOYS,
        OTHER
    }
    
    public enum ProductType {
        REGULAR,
        SUBSCRIPTION
    }
}
