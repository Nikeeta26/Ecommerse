package com.ecommerce.controller;

import com.ecommerce.dto.WishlistDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{productId}")
    public ResponseEntity<WishlistDto> addToWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.addToWishlist(userDetails.getUsername(), productId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        wishlistService.removeFromWishlist(userDetails.getUsername(), productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WishlistDto>> getWishlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(wishlistService.getWishlist(userDetails.getUsername()));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<?> getWishlistItemByProductId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        try {
            WishlistDto wishlistItem = wishlistService.getWishlistItemByProductId(
                userDetails.getUsername(), 
                productId
            );
            return ResponseEntity.ok(wishlistItem);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "not_found",
                "message", "Product not found in wishlist"
            ));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getWishlistCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(wishlistService.getWishlistCount(userDetails.getUsername()));
    }
    
    @GetMapping("/item/{id}")
    public ResponseEntity<WishlistDto> getWishlistItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(wishlistService.getWishlistItemById(userDetails.getUsername(), id));
    }
}



//curl --location --request POST 'http://localhost:8080/api/wishlist/34' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ0d29AZ21haWwuY29tIiwiaWF0IjoxNzU5OTM4NjQ2LCJleHAiOjE3NjAwMjUwNDZ9.TooCSCJPag6IU6hrMUuaimhpRAcbQSQdgv-Nuqx-Fjp5lredtVcSAvvPiDIJ1nMI' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ0d29AZ21haWwuY29tIiwiaWF0IjoxNzU5OTM4NjQ2LCJleHAiOjE3NjAwMjUwNDZ9.TooCSCJPag6IU6hrMUuaimhpRAcbQSQdgv-Nuqx-Fjp5lredtVcSAvvPiDIJ1nMI'


//curl http://localhost:8080/api/wishlist \
//        -H "Authorization: Bearer YOUR_JWT_TOKEN"


//curl -X DELETE http://localhost:8080/api/wishlist/1 \
//        -H "Authorization: Bearer YOUR_JWT_TOKEN"


//curl --location 'http://localhost:8080/api/wishlist/check/30' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ0d29AZ21haWwuY29tIiwiaWF0IjoxNzU5OTQwMDI0LCJleHAiOjE3NjAwMjY0MjR9.6dRW1SGPSTNybNl5E5E23nxujwLVvkdXUt_rJaofJz_5LW7t4fcNymd4ir_t8STd' \
//        --header 'Cookie: jwt=eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ0d29AZ21haWwuY29tIiwiaWF0IjoxNzU5OTQwMDI0LCJleHAiOjE3NjAwMjY0MjR9.6dRW1SGPSTNybNl5E5E23nxujwLVvkdXUt_rJaofJz_5LW7t4fcNymd4ir_t8STd'