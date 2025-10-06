package com.ecommerce.dto;

import com.ecommerce.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequestDTO {
    @NotBlank(message = "Product name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be a positive number")
    private BigDecimal price;
    
    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock must be a positive number")
    private Integer stock;
    
    private String imageUrl;
    
    @NotNull(message = "Category is required")
    private Product.Category category;
    
    private Product.Subcategory subcategory;
    

    public Product toProduct() {
        Product product = new Product();
        product.setName(this.name);
        product.setDescription(this.description);
        product.setPrice(this.price);
        product.setStock(this.stock);
        product.setImageUrl(this.imageUrl);
        product.setCategory(this.category);
        product.setSubcategory(this.subcategory);

        return product;
    }
    
    public void updateProduct(Product product) {
        if (this.name != null) product.setName(this.name);
        if (this.description != null) product.setDescription(this.description);
        if (this.price != null) product.setPrice(this.price);
        if (this.stock != null) product.setStock(this.stock);
        if (this.imageUrl != null) product.setImageUrl(this.imageUrl);
        if (this.category != null) product.setCategory(this.category);
        if (this.subcategory != null) product.setSubcategory(this.subcategory);

    }
}
