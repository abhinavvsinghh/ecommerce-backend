package com.nagarro.amcart.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteriaRequest {
    
    private String keyword;
    
    // Only keeping category and price filters
    private String categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // Pagination
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 20;
    
    // Enable fuzzy search or not
    @Builder.Default
    private boolean fuzzySearch = true;
}