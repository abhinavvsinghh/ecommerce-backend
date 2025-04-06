package com.nagarro.amcart.service;

import com.nagarro.amcart.dto.request.ReviewRequest;
import com.nagarro.amcart.dto.request.SearchCriteriaRequest;
import com.nagarro.amcart.dto.response.ProductResponse;
import com.nagarro.amcart.dto.response.SearchResponse;
import com.nagarro.amcart.model.Product;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(String id);

    List<ProductResponse> getProductsByCategory(String categoryId);
    
    // Get products including all subcategories in the hierarchy
    List<ProductResponse> getProductsInCategoryHierarchy(String categoryId);

    // Basic search by keyword
    List<ProductResponse> searchProducts(String keyword);

    // Advanced search with category and price filters
    SearchResponse advancedSearch(SearchCriteriaRequest searchCriteria);

    List<ProductResponse> getProductsByBrand(String brand);

    List<ProductResponse> getProductsByColor(String color);

    List<ProductResponse> getProductsOnSale();


    // Review methods
    ProductResponse addReview(String productId, String userId, ReviewRequest reviewRequest);

    // ElasticSearch index methods
    void indexProductToElasticSearch(Product product);

    void deleteProductFromElasticSearch(String productId);

    // Reindex all products
    void reindexAllProducts();
}