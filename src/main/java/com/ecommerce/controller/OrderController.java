package com.ecommerce.controller;

import com.ecommerce.dto.OrderDtos;
import com.ecommerce.model.Order;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.OrderService;
import com.ecommerce.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    // User: place order
    @PostMapping("/orders")
    public ResponseEntity<Order> placeOrder(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody OrderDtos.PlaceOrderRequest request) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        var order = orderService.placeOrder(user, request);
        return ResponseEntity.ok(order);
    }

    // User: my orders
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> myOrders(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(orderService.getOrdersForUser(user));
    }

    // Admin: all orders
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders")
    public ResponseEntity<List<Order>> allOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // Admin: update status
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/orders/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id,
                                              @Valid @RequestBody OrderDtos.UpdateStatusRequest req) {
        return ResponseEntity.ok(orderService.updateStatus(id, req.getStatus()));
    }
}
