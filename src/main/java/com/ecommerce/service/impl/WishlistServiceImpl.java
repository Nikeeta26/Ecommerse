package com.ecommerce.service.impl;

import com.ecommerce.dto.WishlistDto;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.model.Wishlist;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
import com.ecommerce.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public WishlistDto addToWishlist(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
                
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Check if the product is already in the user's wishlist (including inactive ones)
        return wishlistRepository.findByUserEmailAndProductId(email, productId)
                .map(existingWishlist -> {
                    // If exists but inactive, reactivate it
                    if (!existingWishlist.isActive()) {
                        existingWishlist.setActive(true);
                        Wishlist savedWishlist = wishlistRepository.save(existingWishlist);
                        return convertToDto(savedWishlist);
                    }
                    // If already active, return as is
                    return convertToDto(existingWishlist);
                })
                .orElseGet(() -> {
                    // If not exists at all, create a new wishlist item
                    Wishlist wishlist = new Wishlist();
                    wishlist.setUser(user);
                    wishlist.setProduct(product);
                    wishlist.setActive(true);
                    Wishlist savedWishlist = wishlistRepository.save(wishlist);
                    return convertToDto(savedWishlist);
                });
    }

    @Override
    @Transactional
    public void removeFromWishlist(String email, Long productId) {
        // Find the wishlist item for the user and product
        Wishlist wishlistItem = wishlistRepository.findByUserEmailAndProductId(email, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found for product id: " + productId));
        
        // Delete the wishlist item by its ID
        wishlistRepository.deleteById(wishlistItem.getId());
    }

    @Override
    public List<WishlistDto> getWishlist(String email) {
        // Only get active wishlist items
        return wishlistRepository.findAllActiveByUserEmail(email).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean isProductInWishlist(String email, Long productId) {
        return wishlistRepository.findActiveByUserEmailAndProductId(email, productId).isPresent();
    }
    
    @Override
    public int getWishlistCount(String email) {
        return wishlistRepository.countByUserEmail(email);
    }
    
    @Override
    public WishlistDto getWishlistItemById(String email, Long id) {
        return wishlistRepository.findByIdAndUserEmail(id, email)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found with id: " + id));
    }
    
    @Override
    public WishlistDto getWishlistItemByProductId(String email, Long productId) {
        return wishlistRepository.findActiveByUserEmailAndProductId(email, productId)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in wishlist"));
    }

    private WishlistDto convertToDto(Wishlist wishlist) {
        WishlistDto dto = new WishlistDto();
        dto.setId(wishlist.getId());
        dto.setProductId(wishlist.getProduct().getId());
        dto.setProductName(wishlist.getProduct().getName());
        dto.setProductPrice(wishlist.getProduct().getPrice());
        dto.setProductImageUrl(wishlist.getProduct().getImageUrl());
        dto.setAddedAt(wishlist.getCreatedAt());
        return dto;
    }
}
