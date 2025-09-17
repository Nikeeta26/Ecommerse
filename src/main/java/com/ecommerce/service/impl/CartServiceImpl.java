package com.ecommerce.service.impl;

import com.ecommerce.dto.CartDtos;
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

import java.math.BigDecimal;
import java.util.Iterator;

@Service
public class CartServiceImpl implements CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;

    private Cart ensureCart(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(user);
            return cartRepository.save(c);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCart(User user) {
        return ensureCart(user);
    }

    @Override
    @Transactional
    public Cart addItem(User user, CartDtos.AddItemRequest request) {
        Cart cart = ensureCart(user);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        // if exists, update qty; else add
        for (CartItem it : cart.getItems()) {
            if (it.getProduct().getId().equals(product.getId())) {
                it.setQuantity(it.getQuantity() + request.getQuantity());
                it.setUnitPrice(product.getPrice());
                return cartRepository.save(cart);
            }
        }
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(product.getPrice());
        cart.getItems().add(item);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart updateQuantity(User user, CartDtos.UpdateQtyRequest request) {
        Cart cart = ensureCart(user);
        for (CartItem it : cart.getItems()) {
            if (it.getProduct().getId().equals(request.getProductId())) {
                it.setQuantity(request.getQuantity());
                return cartRepository.save(cart);
            }
        }
        throw new IllegalArgumentException("Item not in cart");
    }

    @Override
    @Transactional
    public Cart removeItem(User user, Long productId) {
        Cart cart = ensureCart(user);
        Iterator<CartItem> iterator = cart.getItems().iterator();
        while (iterator.hasNext()) {
            CartItem it = iterator.next();
            if (it.getProduct().getId().equals(productId)) {
                iterator.remove();
                break;
            }
        }
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart clear(User user) {
        Cart cart = ensureCart(user);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }
}
