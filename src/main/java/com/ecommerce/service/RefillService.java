package com.ecommerce.service;

import com.ecommerce.dto.RefillRequestDTO;
import com.ecommerce.model.User;

import java.util.Map;

public interface RefillService {
    
    /**
     * Check if a user can request a refill for the given products
     */
    boolean canRequestRefill(RefillRequestDTO refillRequest, User user);
    
    /**
     * Process a refill request
     */
    Map<String, Object> processRefill(RefillRequestDTO refillRequest, User user);
    
    /**
     * Get refill history for a subscription
     */
    Map<String, Object> getRefillHistory(Long subscriptionId, User user);
    
    /**
     * Get list of products that can be refilled by the user
     */
    Map<String, Object> getRefillableProducts(User user);
}
