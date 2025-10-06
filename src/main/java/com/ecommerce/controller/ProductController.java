package com.ecommerce.controller;

import com.ecommerce.dto.ProductRequestDTO;
import com.ecommerce.dto.ProductResponseDTO;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.model.Product.Category;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Public endpoint - get all products without pagination
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.findAll().stream()
                .map(ProductResponseDTO::fromProduct)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ProductResponseDTO::fromProduct)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/products/category/{category}")
    public ResponseEntity<List<ProductResponseDTO>> getByCategory(@PathVariable Category category) {
        List<ProductResponseDTO> products = productService.findByCategory(category).stream()
                .map(ProductResponseDTO::fromProduct)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    // Admin endpoints (secured by JWT role)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/products")
    public ResponseEntity<Page<ProductResponseDTO>> getAllProductsAdmin(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<Product> productsPage = productService.findAll(pageable);
        List<ProductResponseDTO> dtos = productsPage.getContent().stream()
                .map(ProductResponseDTO::fromProduct)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new PageImpl<>(dtos, pageable, productsPage.getTotalElements()));
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/products")
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO productRequest) {
        Product product = productRequest.toProduct();
        Product createdProduct = productService.create(product);
        return ResponseEntity.ok(ProductResponseDTO.fromProduct(createdProduct));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/products/{id}")
    public ResponseEntity<ProductResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO productRequest) {
        return productService.findById(id)
                .map(existingProduct -> {
                    productRequest.updateProduct(existingProduct);
                    return productService.update(id, existingProduct)
                            .map(updatedProduct -> ResponseEntity.ok(ProductResponseDTO.fromProduct(updatedProduct)))
                            .orElse(ResponseEntity.notFound().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}




//-- Drop the column
//ALTER TABLE products DROP COLUMN IF EXISTS product_type;
//ALTER TABLE orders ALTER COLUMN order_type DROP NOT NULL;



//curl --location 'http://localhost:8080/api/products/7'


//curl --location 'http://localhost:8080/api/products/category/ELECTRONICS' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJwcEBnbWFpbC5jb20iLCJpYXQiOjE3NTg5MTE1MDEsImV4cCI6MTc1ODk5NzkwMX0.oDpUAtKkjbPUdBcC6Fo3CFlP5H0TjA7cSHsv-ssbr6prZfp7PkDozfMLW4lgTZc2'

//curl --location --request DELETE 'http://localhost:8080/api/admin/products/2' \
//        --header 'Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJic2JkZmJzZGJiamtAZ21haWwuY29tIiwiaWF0IjoxNzU4NzI5MjU4LCJleHAiOjE3NTg4MTU2NTh9.vsy4jGvfeZRoMFqGV3aS9VpMtQ5EyrMi8wiIoy5bMIu20XLfZQMrtmsOfgvsldgX'