package com.ecommerce.service;

import com.ecommerce.model.Cart;
import com.ecommerce.model.User;

public interface CartService {
    /**
     * Get or create a cart for the user
     */
    Cart getOrCreateUserCart(User user);
    
    /**
     * Add an item to the user's cart
     */
    Cart addItemToCart(User user, Long productId, int quantity);
    
    /**
     * Update the quantity of an item in the cart
     */
    Cart updateCartItem(User user, Long itemId, int quantity);
    
    /**
     * Remove an item from the cart
     */
    Cart removeItemFromCart(User user, Long itemId);
    
    /**
     * Clear all items from the cart
     */
    void clearUserCart(User user);
    
    /**
     * Get cart by ID
     * @param cartId the ID of the cart to retrieve
     * @return the cart with the specified ID
     * @throws com.ecommerce.exception.ResourceNotFoundException if cart is not found
     */
    Cart getCartById(Long cartId);
    
    /**
     * Check if a user is the owner of a cart
     * @param userId the ID of the user
     * @param cartId the ID of the cart
     * @return true if the user is the owner of the cart, false otherwise
     */
    boolean isUserCartOwner(Long userId, Long cartId);
}
