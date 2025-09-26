package com.ecommerce.service.impl;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    @Override
    public List<Product> findByCategory(Product.Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return productRepository.findByCategory(category);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product create(Product product) {
        product.setId(null);
        return productRepository.save(product);
    }

    @Override
    public Optional<Product> update(Long id, Product product) {
        return productRepository.findById(id)
                .map(existing -> {
                    existing.setName(product.getName());
                    existing.setDescription(product.getDescription());
                    existing.setPrice(product.getPrice());
                    existing.setStock(product.getStock());
                    existing.setImageUrl(product.getImageUrl());
                    existing.setCategory(product.getCategory());
//                    existing.setActive(product.getActive());
                    return productRepository.save(existing);
                });
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
