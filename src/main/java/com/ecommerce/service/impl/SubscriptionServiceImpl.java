package com.ecommerce.service.impl;

import java.util.List;
import com.ecommerce.dto.SubscriptionDTO;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.SubscriptionRepository;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.SubscriptionService;
import com.ecommerce.dto.SubscriptionCheckResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;

    @Autowired
    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
                                  ProductRepository productRepository,
                                  OrderService orderService) {
        this.subscriptionRepository = subscriptionRepository;
        this.productRepository = productRepository;
        this.orderService = orderService;
    }

    @Override
    public SubscriptionCheckResponse checkProductSubscription(Long userId, Long productId) {
        // Check if user has an active subscription for this product
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByUserIdAndProductIdAndActiveTrue(userId, productId);
        
        if (subscriptionOpt.isPresent()) {
            return SubscriptionCheckResponse.withSubscription(subscriptionOpt.get().getId());
        }
        
        return SubscriptionCheckResponse.withoutSubscription();
    }
    
    @Override
    @Transactional
    public Subscription createSubscription(SubscriptionDTO subscriptionDTO, User user) {
        // Check for each product in the subscription request
        for (SubscriptionDTO.SubscriptionProductDTO productDTO : subscriptionDTO.getProducts()) {
            // Check if user already has an active subscription for this product
            if (subscriptionRepository.existsByUserIdAndProductIdAndActiveTrue(user.getId(), productDTO.getProductId())) {
                Product product = productRepository.findById(productDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productDTO.getProductId()));
                throw new IllegalStateException("You already have an active subscription for: " + product.getName());
            }
        }

        // Create the subscription
        Subscription subscription = new Subscription();
        LocalDateTime now = LocalDateTime.now();
        int defaultSubscriptionMonths = 12; // Default subscription period in months (1 year)
        
        // Set subscription properties
        subscription.setUser(user);
        subscription.setStartDate(now);
        subscription.setEndDate(now.plusMonths(defaultSubscriptionMonths));
        subscription.setActive(true);
        subscription.setRefillFrequencyDays(30); // Default 30 days between refills
        
        // Initialize products and quantities
        subscription.setProducts(new HashSet<>());
        subscription.setProductQuantities(new HashMap<>());
        
        // Process each product in the subscription
        for (SubscriptionDTO.SubscriptionProductDTO productDTO : subscriptionDTO.getProducts()) {
            Product product = productRepository.findById(productDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productDTO.getProductId()));
                
            // Verify product is reusable
            if (!product.isReusable()) {
                throw new IllegalArgumentException("Product is not a reusable item: " + product.getName());
            }
            
            // Add product to subscription's product set
            subscription.getProducts().add(product);
            
            // Set quantity for the product
            subscription.getProductQuantities().put(product, productDTO.getQuantity());
        }
        
        // Set next refill date
        subscription.setNextRefillDate(now.plusDays(subscription.getRefillFrequencyDays()));
        
        // Save and return the subscription
        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription getSubscription(Long subscriptionId, User user) {
        return subscriptionRepository.findByIdAndUserId(subscriptionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
    }

    @Override
    public List<Subscription> getUserSubscriptions(User user) {
        return subscriptionRepository.findByUserIdAndActiveTrue(user.getId());
    }

    @Override
    @Transactional
    public void cancelSubscription(Long subscriptionId, User user) {
        Subscription subscription = getSubscription(subscriptionId, user);
        subscription.setActive(false);
        subscription.setEndDate(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    public void processRefills() {
        List<Subscription> dueSubscriptions = subscriptionRepository
                .findByNextRefillDateBeforeAndActiveTrue(LocalDateTime.now());

        for (Subscription subscription : dueSubscriptions) {
            try {
                // Create order for refill
                orderService.createRefillOrder(subscription);

                // Update next refill date
                subscription.scheduleNextRefill();
                subscriptionRepository.save(subscription);
            } catch (Exception e) {
                // Log error and continue with next subscription
                // In production, you might want to implement retry logic or notifications
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean canPurchaseRefill(Long productId, Long userId) {
        // If product doesn't require subscription, anyone can purchase
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.isReusable() || !product.isRequiresSubscription()) {
            return true;
        }

        // Get all active subscriptions for the user
        List<Subscription> activeSubscriptions = subscriptionRepository.findByUserIdAndActiveTrue(userId);
        
        // Check if any of the active subscriptions include this product
        return activeSubscriptions.stream()
            .flatMap(subscription -> subscription.getProducts().stream())
            .anyMatch(p -> p.getId().equals(productId));
    }

    @Override
    @Transactional
    public boolean requestRefill(Long subscriptionId, User user) {
        Subscription subscription = getSubscription(subscriptionId, user);
        
        if (!subscription.isActive()) {
            throw new IllegalStateException("Subscription is not active");
        }
        
        if (!subscription.isEligibleForRefill()) {
            return false; // Not yet eligible for refill
        }
        
        // Create order for refill
        orderService.createRefillOrder(subscription);
        
        // Update next refill date
        subscription.scheduleNextRefill();
        subscriptionRepository.save(subscription);
        
        return true;
    }
}
