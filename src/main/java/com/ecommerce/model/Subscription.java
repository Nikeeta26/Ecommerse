package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class Subscription extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "subscription_products",
        joinColumns = @JoinColumn(name = "subscription_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();
    
    @Column(nullable = false)
    private LocalDateTime startDate;
    
    @Column(nullable = false)
    private LocalDateTime endDate;
    
    @Column(nullable = false)
    private boolean active;
    
    @ElementCollection
    @CollectionTable(name = "subscription_product_quantities", 
                    joinColumns = @JoinColumn(name = "subscription_id"))
    @MapKeyJoinColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Product, Integer> productQuantities = new HashMap<>();
    
    @Column(nullable = false)
    private int refillFrequencyDays;
    
    @Column
    private LocalDateTime nextRefillDate;
    
    @Version
    private Long version;
    
    public boolean isEligibleForRefill() {
        return active && LocalDateTime.now().isAfter(nextRefillDate);
    }
    
    public void scheduleNextRefill() {
        if (active) {
            this.nextRefillDate = LocalDateTime.now().plusDays(refillFrequencyDays);
        }
    }
}
