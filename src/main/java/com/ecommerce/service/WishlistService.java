package com.ecommerce.service;

import com.ecommerce.dto.WishlistDto;
import com.ecommerce.model.User;

public interface WishlistService {
    /**
     * Get user's wishlist
     */
    WishlistDto getWishlist(User user);
    
    /**
     * Add product to wishlist
     */
    WishlistDto addToWishlist(User user, Long productId);
    
    /**
     * Remove product from wishlist
     */
    void removeFromWishlist(User user, Long productId);
    
    /**
     * Check if a product is in user's wishlist
     */
    boolean isProductInWishlist(User user, Long productId);
}
