package com.ecommerce.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer stock;
    
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subcategory")
    @Basic(fetch = FetchType.EAGER)
    private Subcategory subcategory;
    
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
    
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
    
    public enum Subcategory {
        // Electronics subcategories
        SMARTPHONES,
        LAPTOPS,
        HEADPHONES,
        
        // Clothing subcategories
        MEN,
        WOMEN,
        KIDS,
        
        // Books subcategories
        FICTION,
        NON_FICTION,
        EDUCATIONAL,
        
        // Home Appliances subcategories
        KITCHEN,
        CLEANING,
        
        // Sports subcategories
        FITNESS,
        OUTDOOR,
        
        // Beauty subcategories
        SKINCARE,
        MAKEUP,
        
        // Toys subcategories
        BOARD_GAMES,
        OUTDOOR_TOYS,
        
        // Default
        OTHER
    }
}
