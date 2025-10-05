package com.ecommerce.service.impl;

import com.ecommerce.dto.WishlistDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.model.Wishlist;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.WishlistRepository;
import com.ecommerce.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public WishlistDto getWishlist(User user) {
        log.info("Fetching wishlist for user: {}", user.getId());
        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> createWishlist(user));
        return WishlistDto.fromWishlist(wishlist);
    }

    @Override
    @Transactional
    public WishlistDto addToWishlist(User user, Long productId) {
        log.info("Adding product {} to wishlist for user: {}", productId, user.getId());
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> createWishlist(user));

        if (!wishlist.getProducts().contains(product)) {
            wishlist.addProduct(product);
            wishlist = wishlistRepository.save(wishlist);
            log.info("Product {} added to wishlist for user: {}", productId, user.getId());
        }

        return WishlistDto.fromWishlist(wishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(User user, Long productId) {
        log.info("Removing product {} from wishlist for user: {}", productId, user.getId());
        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found for user: " + user.getId()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (wishlist.getProducts().contains(product)) {
            wishlist.removeProduct(product);
            wishlistRepository.save(wishlist);
            log.info("Product {} removed from wishlist for user: {}", productId, user.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductInWishlist(User user, Long productId) {
        return wishlistRepository.findByUserId(user.getId())
                .map(wishlist -> wishlist.getProducts().stream()
                        .anyMatch(product -> product.getId().equals(productId)))
                .orElse(false);
    }

    private Wishlist createWishlist(User user) {
        log.info("Creating new wishlist for user: {}", user.getId());
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        return wishlistRepository.save(wishlist);
    }
}
