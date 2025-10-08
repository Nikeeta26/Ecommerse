package com.ecommerce.service;

import com.ecommerce.dto.WishlistDto;

import java.util.List;

public interface WishlistService {
    WishlistDto addToWishlist(String email, Long productId);
    void removeFromWishlist(String email, Long productId);
    List<WishlistDto> getWishlist(String email);
    boolean isProductInWishlist(String email, Long productId);
    int getWishlistCount(String email);
    
    /**
     * Get a wishlist item by ID for the specified user
     * @param email User's email
     * @param id Wishlist item ID
     * @return WishlistDto if found, throws ResourceNotFoundException otherwise
     */
    WishlistDto getWishlistItemById(String email, Long id);
    
    /**
     * Get wishlist item by product ID for the specified user
     * @param email User's email
     * @param productId Product ID to check
     * @return WishlistDto if found, throws ResourceNotFoundException otherwise
     */
    WishlistDto getWishlistItemByProductId(String email, Long productId);
}
