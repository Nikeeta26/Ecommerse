package com.ecommerce.service;

import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductService {
    Page<Product> findAll(Pageable pageable);
    Optional<Product> findById(Long id);
    Product create(Product product);
    Product update(Long id, Product product);
    void delete(Long id);
}
