package com.ecommerce.service.impl;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, 
                          CartItemRepository cartItemRepository,
                          ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getOrCreateUserCart(User user) {
        return cartRepository.findByUser(user)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });
    }

    @Override
    public Cart addItemToCart(User user, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        
        Cart cart = getOrCreateUserCart(user);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        // Check if product already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity if item already exists
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getPrice());
            cart.getItems().add(newItem);
        }
        
        return cartRepository.save(cart);
    }

    @Override
    public Cart updateCartItem(User user, Long itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        
        Cart cart = getOrCreateUserCart(user);
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Item does not belong to user's cart");
        }
        
        item.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    @Override
    public Cart removeItemFromCart(User user, Long itemId) {
        Cart cart = getOrCreateUserCart(user);
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Item does not belong to user's cart");
        }
        
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        cartItemRepository.delete(item);
        
        return cartRepository.save(cart);
    }

    @Override
    public void clearUserCart(User user) {
        Cart cart = getOrCreateUserCart(user);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
