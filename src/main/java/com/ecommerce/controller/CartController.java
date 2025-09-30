package com.ecommerce.controller;

import com.ecommerce.dto.CartDtos;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @Autowired
    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<CartDtos.CartResponse> getActiveCart(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartService.getOrCreateUserCart(user);
        return ResponseEntity.ok(mapToCartResponse(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDtos.CartResponse> addItemToCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CartDtos.AddItemRequest request) {

        var user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart updatedCart = cartService.addItemToCart(user, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(mapToCartResponse(updatedCart));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDtos.CartResponse> updateCartItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId,
            @Valid @RequestBody CartDtos.UpdateQtyRequest request) {

        var user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart updatedCart = cartService.updateCartItem(user, itemId, request.getQuantity());
        return ResponseEntity.ok(mapToCartResponse(updatedCart));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDtos.CartResponse> removeItemFromCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId) {

        var user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart updatedCart = cartService.removeItemFromCart(user, itemId);
        return ResponseEntity.ok(mapToCartResponse(updatedCart));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        cartService.clearUserCart(user);
        return ResponseEntity.noContent().build();
    }

    private CartDtos.CartResponse mapToCartResponse(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartDtos.CartResponse response = new CartDtos.CartResponse();
        response.setId(cart.getId());
        response.setTotal(cart.getTotal());
        response.setTotalItems(cart.getItems() != null ? cart.getItems().size() : 0);

        // Map cart items to response DTOs
        if (cart.getItems() != null) {
            List<CartDtos.CartItemResponse> itemResponses = cart.getItems().stream()
                    .map(CartDtos.CartItemResponse::fromEntity)
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        }

        return response;
    }
}





//curl --location 'http://localhost:8080/api/carts/items' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTkyNDk4MTEsImV4cCI6MTc1OTMzNjIxMX0.wFdDk1Mt2lXMce1XcBmofBHrp9jHpOojevh5dLbQEL-WBPIwB59JPE3TRJ9i0G5G' \
//        --header 'Content-Type: application/json' \
//        --data '{
//        "productId": 13,
//        "quantity": 2
//        }'




//curl --location --request PUT 'http://localhost:8080/api/carts/items/1' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTkyNDk4MTEsImV4cCI6MTc1OTMzNjIxMX0.wFdDk1Mt2lXMce1XcBmofBHrp9jHpOojevh5dLbQEL-WBPIwB59JPE3TRJ9i0G5G' \
//        --header 'Content-Type: application/json' \
//        --data '{
//        "quantity": 5,
//        "productId": 13
//        }'


//curl --location --request DELETE 'http://localhost:8080/api/carts/items/1' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTkyNDk4MTEsImV4cCI6MTc1OTMzNjIxMX0.wFdDk1Mt2lXMce1XcBmofBHrp9jHpOojevh5dLbQEL-WBPIwB59JPE3TRJ9i0G5G'



//
//curl --location 'http://localhost:8080/api/carts' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTkyNDk4MTEsImV4cCI6MTc1OTMzNjIxMX0.wFdDk1Mt2lXMce1XcBmofBHrp9jHpOojevh5dLbQEL-WBPIwB59JPE3TRJ9i0G5G'

//curl --location --request POST 'http://localhost:8080/api/auth/logout' \
//        --header 'Content-Type: application/json'


//
//curl --location 'http://localhost:8080/api/orders' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTkyNDk4MTEsImV4cCI6MTc1OTMzNjIxMX0.wFdDk1Mt2lXMce1XcBmofBHrp9jHpOojevh5dLbQEL-WBPIwB59JPE3TRJ9i0G5G' \
//        --header 'Content-Type: application/json' \
//        --data '{
//
//
//        "shippingAddressId": 17,
//        "paymentMethod": "CREDIT_CARD",
//        "notes": "Please deliver after 5 PM",
//        "items": [
//        {
//        "id": 4,
//        "productId": 4,
//        "productName": "Sample Phone",
//        "unitPrice": 499.99,
//        "quantity": 4,
//        "subtotal": 1999.96
//        },
//        {
//        "id": 5,
//        "productId": 5,
//        "productName": "Sample Phone",
//        "unitPrice": 499.99,
//        "quantity": 2,
//        "subtotal": 999.98
//        }
//        ],
//        "total": 2999.94,
//        "totalItems": 2
//
//        }'