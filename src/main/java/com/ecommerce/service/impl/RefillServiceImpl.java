package com.ecommerce.service.impl;

import com.ecommerce.dto.OrderDtos;
import com.ecommerce.dto.RefillRequestDTO;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InvalidOrderException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.SubscriptionRepository;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.RefillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RefillServiceImpl implements RefillService {

    private final SubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;

    @Autowired
    public RefillServiceImpl(SubscriptionRepository subscriptionRepository,
                           ProductRepository productRepository,
                           OrderService orderService) {
        this.subscriptionRepository = subscriptionRepository;
        this.productRepository = productRepository;
        this.orderService = orderService;
    }

    @Override
    public boolean canRequestRefill(RefillRequestDTO refillRequest, User user) {
        if (refillRequest == null || user == null || refillRequest.getSubscriptionId() == null) {
            log.warn("Invalid refill request or user");
            return false;
        }

        // Find the subscription
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(refillRequest.getSubscriptionId())
            .filter(sub -> sub.getUser().getId().equals(user.getId()) && sub.isActive());

        if (subscriptionOpt.isEmpty()) {
            log.warn("No active subscription found for user: {}, subscription: {}", user.getId(), refillRequest.getSubscriptionId());
            return false;
        }

        Subscription subscription = subscriptionOpt.get();
        Map<Product, Integer> subscribedProducts = subscription.getProductQuantities();
        
        if (subscribedProducts.isEmpty()) {
            log.warn("No products found in subscription: {}", subscription.getId());
            return false;
        }

        // Check if all requested products are in the subscription
        Set<Long> subscribedProductIds = subscribedProducts.keySet().stream()
            .map(Product::getId)
            .collect(Collectors.toSet());

        boolean allProductsInSubscription = refillRequest.getItems().stream()
            .allMatch(item -> subscribedProductIds.contains(item.getProductId()));

        if (!allProductsInSubscription) {
            log.warn("Not all products are part of subscription: {}", refillRequest.getItems());
        }

        return allProductsInSubscription;
    }

    @Override
    @Transactional
    public Map<String, Object> processRefill(RefillRequestDTO refillRequest, User user) {
        log.info("Processing refill request for user: {}, subscription: {}", user.getId(), refillRequest.getSubscriptionId());
        
        // Validate inputs
        if (refillRequest == null || user == null) {
            throw new IllegalArgumentException("Refill request and user cannot be null");
        }

        // Get and validate subscription
        Subscription subscription = subscriptionRepository.findById(refillRequest.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + refillRequest.getSubscriptionId()));

        // Verify ownership and active status
        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You don't have permission to access this subscription");
        }

        if (!subscription.isActive()) {
            throw new IllegalStateException("This subscription is not active");
        }
        
        // Prepare order request
        OrderDtos.PlaceOrderRequest orderRequest = new OrderDtos.PlaceOrderRequest();
        orderRequest.setShippingAddressId(refillRequest.getShippingAddressId());
        orderRequest.setOrderType(Order.OrderType.REFILL);
        
        // Convert refill items to order items
        List<OrderDtos.OrderItemRequest> orderItems = refillRequest.getItems().stream()
            .map(item -> {
                OrderDtos.OrderItemRequest orderItem = new OrderDtos.OrderItemRequest();
                orderItem.setProductId(item.getProductId());
                orderItem.setQuantity(item.getQuantity());
                return orderItem;
            })
            .collect(Collectors.toList());
        
        orderRequest.setItems(orderItems);
        
        try {
            // Delegate to order service
            Order order = orderService.placeOrder(user, orderRequest);
            
            // Update subscription with new refill dates
            subscription.scheduleNextRefill();
            subscription = subscriptionRepository.save(subscription);
            
            // Convert order to response DTO
            OrderDtos.OrderResponse orderResponse = OrderDtos.OrderResponse.fromEntity(order);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Refill order placed successfully");
            response.put("id", orderResponse.getId());
            response.put("orderNumber", orderResponse.getOrderNumber());
            response.put("status", orderResponse.getStatus() != null ? orderResponse.getStatus().name() : null);
            response.put("orderDate", orderResponse.getOrderDate());
            response.put("totalAmount", orderResponse.getTotal());
            response.put("nextRefillDate", subscription.getNextRefillDate());
            response.put("shippingAddressId", orderResponse.getShippingAddressId());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing refill order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process refill: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getRefillHistory(Long subscriptionId, User user) {
        log.info("Fetching refill history for subscription: {}, user: {}", subscriptionId, user.getId());
        
        if (subscriptionId == null || user == null) {
            throw new IllegalArgumentException("Subscription ID and user cannot be null");
        }

        // Get the subscription and verify ownership
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You don't have permission to access this subscription");
        }

        try {
            // Get all refill orders for this subscription
            List<Order> refillOrders = orderService.findRefillOrdersBySubscription(subscriptionId);

            // Format the response
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptionId", subscriptionId);
            response.put("subscriptionStatus", subscription.isActive() ? "ACTIVE" : "INACTIVE");
            response.put("nextRefillDate", subscription.getNextRefillDate());
            response.put("refillHistory", refillOrders.stream()
                    .map(order -> {
                        Map<String, Object> orderMap = new HashMap<>();
                        orderMap.put("orderId", order.getId());
                        orderMap.put("orderNumber", order.getOrderNumber());
                        orderMap.put("orderDate", order.getOrderDate());
                        orderMap.put("status", order.getStatus().name());
                        orderMap.put("totalAmount", order.getTotalAmount());
                        orderMap.put("itemCount", order.getOrderItems().size());
                        return orderMap;
                    })
                    .collect(Collectors.toList()));

            log.info("Found {} refill orders for subscription: {}", refillOrders.size(), subscriptionId);
            return response;
            
        } catch (Exception e) {
            log.error("Error fetching refill history: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch refill history: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getRefillableProducts(User user) {
        log.info("Fetching refillable products for user: {}", user != null ? user.getId() : "null");
        
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and user ID cannot be null");
        }

        // Get user's active subscriptions
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdAndActiveTrue(user.getId());

        if (subscriptions == null || subscriptions.isEmpty()) {
            return Map.of(
                "hasSubscription", false,
                "message", "No active subscription found",
                "subscriptions", List.of()
            );
        }

        List<Map<String, Object>> subscriptionList = new ArrayList<>();

        for (Subscription subscription : subscriptions) {
            if (subscription == null || !subscription.isActive()) {
                continue;
            }

            // Get all reusable products from the subscription
            Map<Product, Integer> subscribedProducts = subscription.getProductQuantities();
            List<Map<String, Object>> products = new ArrayList<>();

            for (Map.Entry<Product, Integer> entry : subscribedProducts.entrySet()) {
                Product product = entry.getKey();
                Integer quantity = entry.getValue();

                // Check if product is reusable and requires subscription using getter methods
                if (product != null && product.isReusable() && product.isRequiresSubscription()) {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("productId", product.getId());
                    productMap.put("name", product.getName());
                    productMap.put("description", product.getDescription());
                    productMap.put("refillPrice", product.getRefillPrice());
                    productMap.put("currentStock", product.getStock());
                    productMap.put("maxQuantity", quantity);
                    products.add(productMap);
                }
            }
            
            // Only include subscriptions with refillable products
            if (!products.isEmpty()) {
                Map<String, Object> subscriptionInfo = new HashMap<>();
                subscriptionInfo.put("subscriptionId", subscription.getId());
                subscriptionInfo.put("nextRefillDate", subscription.getNextRefillDate());
                subscriptionInfo.put("products", products);
                subscriptionList.add(subscriptionInfo);
            }
        }
        
        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("hasSubscription", !subscriptionList.isEmpty());
        response.put("subscriptions", subscriptionList);
        
        if (subscriptionList.isEmpty()) {
            response.put("message", "No refillable products found in your subscriptions");
        }
        
        return response;
    }
}





//curl --location 'http://localhost:8080/api/refills' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NDMyMjIsImV4cCI6MTc1OTcyOTYyMn0.Qz2TVHRh3JA850HW3_0PXIwb57V5KgJYu0MGuB0NePyVVWy-kF2TpAbJ14Ege29K' \
//        --header 'Content-Type: application/json' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NDUyNjksImV4cCI6MTc1OTczMTY2OX0.58utGKdn403Vk4KrW9IErI8Dz4xShaP7b6j3QnZ8nh_p-CdvTZJUGhJhIwnDvkLJ' \
//        --data '{
//        "subscriptionId": 8,
//        "shippingAddressId": 19,
//        "items": [
//        {
//        "productId": 33,
//        "quantity": 2
//        }
//        ]
//        }'