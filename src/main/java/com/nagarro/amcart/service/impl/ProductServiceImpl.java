package com.nagarro.amcart.service.impl;

import com.nagarro.amcart.dto.request.ReviewRequest;
import com.nagarro.amcart.dto.request.SearchCriteriaRequest;
import com.nagarro.amcart.dto.response.ProductResponse;
import com.nagarro.amcart.dto.response.SearchResponse;
import com.nagarro.amcart.exception.ResourceNotFoundException;
import com.nagarro.amcart.model.Category;
import com.nagarro.amcart.model.Product;
import com.nagarro.amcart.model.Review;
import com.nagarro.amcart.model.User;
import com.nagarro.amcart.model.elasticsearch.ESProduct;
import com.nagarro.amcart.repository.CategoryRepository;
import com.nagarro.amcart.repository.ProductRepository;
import com.nagarro.amcart.repository.UserRepository;
import com.nagarro.amcart.repository.elasticsearch.ESProductRepository;
import com.nagarro.amcart.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ESProductRepository esProductRepository;

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToProductResponse(product);
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsInCategoryHierarchy(String categoryId) {
        // First, collect all subcategory IDs in the hierarchy
        Set<String> categoryIds = new HashSet<>();
        categoryIds.add(categoryId); // Include the current category

        // Get subcategories recursively
        collectSubcategoryIds(categoryId, categoryIds);

        // Get products from all collected categories
        List<Product> products = new ArrayList<>();
        for (String id : categoryIds) {
            products.addAll(productRepository.findByCategoryId(id));
        }

        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    private void collectSubcategoryIds(String parentId, Set<String> collectedIds) {
        List<Category> subcategories = categoryRepository.findByParentId(parentId);

        for (Category subcategory : subcategories) {
            collectedIds.add(subcategory.getId());
            // Recursive call to get deeper levels
            collectSubcategoryIds(subcategory.getId(), collectedIds);
        }
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        // Use basic ElasticSearch search
        List<ESProduct> esProducts = esProductRepository.search(keyword);

        // Map ElasticSearch results to full Products from MongoDB
        return esProducts.stream()
                .map(esProduct -> {
                    // Get the full product from MongoDB to include all details
                    return productRepository.findById(esProduct.getId())
                            .map(this::mapToProductResponse)
                            .orElse(null);
                })
                .filter(product -> product != null)
                .collect(Collectors.toList());
    }

    @Override
    public SearchResponse advancedSearch(SearchCriteriaRequest searchCriteria) {
        // Use simplified search with only category and price filters
        SearchResponse searchResponse = esProductRepository.searchProductsWithoutFacets(searchCriteria);

        // Get full products from MongoDB based on IDs from ElasticSearch results
        Page<ESProduct> esProductPage = esProductRepository.searchProducts(searchCriteria);

        List<ProductResponse> productResponses = esProductPage.getContent().stream()
                .map(esProduct -> {
                    return productRepository.findById(esProduct.getId())
                            .map(this::mapToProductResponse)
                            .orElse(null);
                })
                .filter(product -> product != null)
                .collect(Collectors.toList());

        // Set the full product responses in the search response
        searchResponse.setProducts(productResponses);

        return searchResponse;
    }

    @Override
    public List<ProductResponse> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByColor(String color) {
        return productRepository.findByColor(color).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsOnSale() {
        return productRepository.findByOnSaleTrue().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse addReview(String productId, String userId, ReviewRequest reviewRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Create new review
        Review review = Review.builder()
                .userId(userId)
                .userName(user.getEmail()) // Using email as name for simplicity
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .createdAt(new Date())
                .build();

        // Add review to product
        if (product.getReviews() == null) {
            product.setReviews(new ArrayList<>());
        }
        product.getReviews().add(review);

        // Update average rating
        int totalRatings = product.getReviews().stream()
                .mapToInt(Review::getRating)
                .sum();

        product.setReviewCount(product.getReviews().size());
        product.setAverageRating((double) totalRatings / product.getReviewCount());

        // Save updated product
        Product updatedProduct = productRepository.save(product);

        // Update ElasticSearch index
        indexProductToElasticSearch(updatedProduct);

        return mapToProductResponse(updatedProduct);
    }

    @Override
    public void indexProductToElasticSearch(Product product) {
        String categoryName = "";
        if (product.getCategoryId() != null) {
            categoryName = categoryRepository.findById(product.getCategoryId())
                    .map(Category::getName)
                    .orElse("");
        }

        ESProduct esProduct = ESProduct.builder()
                .id(product.getId())
                .name(product.getName())
                .nameKeyword(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .brand(product.getBrand())
                .brandKeyword(product.getBrand())
                .color(product.getColor())
                .colorKeyword(product.getColor())
                .sizes(product.getSizes())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .categoryNameKeyword(categoryName)
                .inStock(product.isInStock())
                .onSale(product.isOnSale())
                .discountPercentage(product.getDiscountPercentage())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();

        esProductRepository.save(esProduct);
        log.info("Product indexed to ElasticSearch: {}", product.getId());
    }

    @Override
    public void deleteProductFromElasticSearch(String productId) {
        esProductRepository.deleteById(productId);
        log.info("Product deleted from ElasticSearch: {}", productId);
    }

    @Override
    public void reindexAllProducts() {
        log.info("Starting to reindex all products to ElasticSearch");

        List<Product> allProducts = productRepository.findAll();
        int count = 0;

        for (Product product : allProducts) {
            try {
                indexProductToElasticSearch(product);
                count++;
            } catch (Exception e) {
                log.error("Failed to index product: {}", product.getId(), e);
            }
        }

        log.info("Finished reindexing {} products to ElasticSearch", count);
    }

    private ProductResponse mapToProductResponse(Product product) {
        String categoryName = "";
        if (product.getCategoryId() != null) {
            categoryName = categoryRepository.findById(product.getCategoryId())
                    .map(Category::getName)
                    .orElse("");
        }

        BigDecimal finalPrice = product.getPrice();
        if (product.isOnSale() && product.getDiscountPercentage() != null) {
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    product.getDiscountPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            finalPrice = product.getPrice().multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .brand(product.getBrand())
                .color(product.getColor())
                .sizes(product.getSizes())
                .images(product.getImages())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .inStock(product.isInStock())
                .onSale(product.isOnSale())
                .discountPercentage(product.getDiscountPercentage())
                .finalPrice(finalPrice)
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .reviews(product.getReviews())
                .build();
    }
}