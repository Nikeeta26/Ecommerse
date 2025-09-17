package com.ecommerce.service;

import com.ecommerce.dto.OrderDtos;
import com.ecommerce.model.Order;
import com.ecommerce.model.User;

import java.util.List;

public interface OrderService {
    Order placeOrder(User user, OrderDtos.PlaceOrderRequest request);
    List<Order> getOrdersForUser(User user);
    List<Order> getAllOrders();
    Order updateStatus(Long orderId, Order.OrderStatus status);
}
