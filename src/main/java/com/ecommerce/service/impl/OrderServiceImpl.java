package com.ecommerce.service.impl;

import com.ecommerce.dto.OrderDtos;
import com.ecommerce.model.Address;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    @Transactional
    public Order placeOrder(User user, OrderDtos.PlaceOrderRequest request) {
        Address shipping = addressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new IllegalArgumentException("Shipping address not found"));
        if (!shipping.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Shipping address does not belong to user");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shipping);
        order.setStatus(Order.OrderStatus.PENDING);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderDtos.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemReq.getProductId()));
            if (product.getStock() < itemReq.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            // decrement stock
            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepository.save(product);

            OrderItem oi = new OrderItem();
            oi.setProduct(product);
            oi.setQuantity(itemReq.getQuantity());
            oi.setUnitPrice(product.getPrice());
            oi.setOrder(order);
            items.add(oi);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalAmount(total);
        order = orderRepository.save(order);
        for (OrderItem oi : items) {
            oi.setOrder(order);
        }
        orderItemRepository.saveAll(items);
        order.setOrderItems(items);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Order updateStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
