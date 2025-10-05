package com.ecommerce.repository;

import com.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface OrderRepositoryCustom {
    List<Order> findBySubscriptionIdAndType(Long subscriptionId, Order.OrderType type, Sort sort);
    Page<Order> findBySubscriptionIdAndType(Long subscriptionId, Order.OrderType type, Pageable pageable);
}
