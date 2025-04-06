package com.nagarro.amcart.dto.response;

import com.nagarro.amcart.model.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String brand;
    private String color;
    private List<String> sizes;
    private List<String> images;
    private String categoryId;
    private String categoryName;
    private boolean inStock;
    private boolean onSale;
    private BigDecimal discountPercentage;
    private BigDecimal finalPrice;

    // Rating information
    private double averageRating;
    private int reviewCount;
    private List<Review> reviews;
}