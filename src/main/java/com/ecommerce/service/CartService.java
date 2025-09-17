package com.ecommerce.service;

import com.ecommerce.dto.CartDtos;
import com.ecommerce.model.Cart;
import com.ecommerce.model.User;

public interface CartService {
    Cart getCart(User user);
    Cart addItem(User user, CartDtos.AddItemRequest request);
    Cart updateQuantity(User user, CartDtos.UpdateQtyRequest request);
    Cart removeItem(User user, Long productId);
    Cart clear(User user);
}
