package com.ecommerce.service;

import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> findAll();
    Page<Product> findAll(Pageable pageable);
    Optional<Product> findById(Long id);
    List<Product> findByCategory(Product.Category category);
    Product create(Product product);
    Optional<Product> update(Long id, Product product);
    void delete(Long id);
}
