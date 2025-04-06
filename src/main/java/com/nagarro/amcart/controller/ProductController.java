package com.nagarro.amcart.controller;

import com.nagarro.amcart.dto.request.ReviewRequest;
import com.nagarro.amcart.dto.request.SearchCriteriaRequest;
import com.nagarro.amcart.dto.response.ProductResponse;
import com.nagarro.amcart.dto.response.SearchResponse;
import com.nagarro.amcart.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String categoryId) {
        List<ProductResponse> products = productService.getProductsInCategoryHierarchy(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        List<ProductResponse> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/search/advanced")
    public ResponseEntity<SearchResponse> advancedSearch(@RequestBody SearchCriteriaRequest searchCriteria) {
        SearchResponse searchResponse = productService.advancedSearch(searchCriteria);
        return ResponseEntity.ok(searchResponse);
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<SearchResponse> advancedSearchGet(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "true") boolean fuzzySearch) {

        SearchCriteriaRequest criteria = SearchCriteriaRequest.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .page(page)
                .size(size)
                .fuzzySearch(fuzzySearch)
                .build();

        SearchResponse searchResponse = productService.advancedSearch(criteria);
        return ResponseEntity.ok(searchResponse);
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<ProductResponse>> getProductsByBrand(@PathVariable String brand) {
        List<ProductResponse> products = productService.getProductsByBrand(brand);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/color/{color}")
    public ResponseEntity<List<ProductResponse>> getProductsByColor(@PathVariable String color) {
        List<ProductResponse> products = productService.getProductsByColor(color);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/sale")
    public ResponseEntity<List<ProductResponse>> getProductsOnSale() {
        List<ProductResponse> products = productService.getProductsOnSale();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ProductResponse> addReview(
            @PathVariable String id,
            @Valid @RequestBody ReviewRequest reviewRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // This will be the user's ID or email

        ProductResponse updatedProduct = productService.addReview(id, userId, reviewRequest);
        return ResponseEntity.ok(updatedProduct);
    }

    @PostMapping("/reindex")
    public ResponseEntity<String> reindexAllProducts() {
        productService.reindexAllProducts();
        return ResponseEntity.ok("All products have been reindexed in ElasticSearch");
    }
}