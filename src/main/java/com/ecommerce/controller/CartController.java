package com.ecommerce.controller;

import com.ecommerce.dto.CartDtos;
import com.ecommerce.model.Cart;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired private CartService cartService;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Cart> get(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(@AuthenticationPrincipal UserPrincipal principal,
                                        @Valid @RequestBody CartDtos.AddItemRequest req) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(cartService.addItem(user, req));
    }

    @PutMapping("/items")
    public ResponseEntity<Cart> updateQty(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody CartDtos.UpdateQtyRequest req) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(cartService.updateQuantity(user, req));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Cart> removeItem(@AuthenticationPrincipal UserPrincipal principal,
                                           @PathVariable Long productId) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(cartService.removeItem(user, productId));
    }

    @DeleteMapping
    public ResponseEntity<Cart> clear(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(cartService.clear(user));
    }
}
