package com.ecommerce.dto;

import com.ecommerce.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDto {
    private Long id;
    private Long userId;
    private Set<WishlistItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WishlistItemDto {
        private Long productId;
        private String productName;
        private Double price;
        private String imageUrl;
        private LocalDateTime addedAt;

        public static WishlistItemDto fromProduct(Product product) {
            return WishlistItemDto.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice().doubleValue())  // Convert BigDecimal to Double
                    .imageUrl(product.getImageUrl())
                    .addedAt(LocalDateTime.now())
                    .build();
        }
    }

    public static WishlistDto fromWishlist(com.ecommerce.model.Wishlist wishlist) {
        return WishlistDto.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser().getId())
                .items(wishlist.getProducts().stream()
                        .map(WishlistItemDto::fromProduct)
                        .collect(Collectors.toSet()))
                .createdAt(wishlist.getCreatedAt())
                .updatedAt(wishlist.getUpdatedAt())
                .build();
    }
}
