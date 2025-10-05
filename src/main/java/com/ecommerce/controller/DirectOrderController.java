package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.DirectOrderRequest;
import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/direct")
@RequiredArgsConstructor
public class DirectOrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse> placeDirectOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DirectOrderRequest request) {
        
        Order order = orderService.placeDirectOrder(user, request);
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Order placed successfully",
            order
        ));
    }
}


//curl --location 'http://localhost:8080/api/orders/buy-now' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NDMyMjIsImV4cCI6MTc1OTcyOTYyMn0.Qz2TVHRh3JA850HW3_0PXIwb57V5KgJYu0MGuB0NePyVVWy-kF2TpAbJ14Ege29K' \
//        --header 'Content-Type: application/json' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtYW5qdUBnbWFpbC5jb20iLCJpYXQiOjE3NTk2NTE5ODUsImV4cCI6MTc1OTczODM4NX0.HaX82MAmEa7C6N4Kb3U_pKD5xfH4LZ0pIyEsmIW11jsewylXhyEJnbgaJZMsK49h' \
//        --data '{
//        "productId": 29,
//        "quantity": 1,
//        "shippingAddressId": 19
//        }'