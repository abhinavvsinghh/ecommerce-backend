package com.nagarro.amcart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    
    private String name;
    private String description;
    private BigDecimal price;
    private String brand;
    private String color;
    private List<String> sizes;
    private List<String> images;
    private String categoryId;
    private int stockQuantity;
    private boolean inStock;
    private Date createdAt;
    private Date updatedAt;
    private boolean onSale;
    private BigDecimal discountPercentage;
    
    // Rating information
    private double averageRating;
    private int reviewCount;
    private List<Review> reviews = new ArrayList<>();
}