package com.ecommerce.service;

import com.ecommerce.dto.SubscriptionDTO;
import com.ecommerce.dto.SubscriptionCheckResponse;
import com.ecommerce.model.Subscription;
import com.ecommerce.model.User;

import java.util.List;

public interface SubscriptionService {
    
    /**
     * Create a new subscription for a reusable product
     * @param subscriptionDTO Subscription details
     * @param user The subscribing user
     * @return The created subscription
     */
    Subscription createSubscription(SubscriptionDTO subscriptionDTO, User user);
    
    /**
     * Get a specific subscription by ID
     */
    Subscription getSubscription(Long subscriptionId, User user);
    
    /**
     * Get all active subscriptions for a user
     */
    List<Subscription> getUserSubscriptions(User user);
    
    /**
     * Check if a user has an active subscription for a specific product
     * @param userId The ID of the user
     * @param productId The ID of the product to check
     * @return SubscriptionCheckResponse with subscription status
     */
    SubscriptionCheckResponse checkProductSubscription(Long userId, Long productId);
    
    /**
     * Cancel an active subscription
     */
    void cancelSubscription(Long subscriptionId, User user);
    
    /**
     * Process all due refills (scheduled task)
     */
    void processRefills();
    
    /**
     * Request a refill for a subscription
     * @return true if refill was successful, false if not eligible yet
     */
    boolean requestRefill(Long subscriptionId, User user);
    
    /**
     * Check if a user can purchase a refill for a product
     * @param productId The product ID
     * @param userId The user ID
     * @return true if user has an active subscription for the product
     */
    boolean canPurchaseRefill(Long productId, Long userId);
}
