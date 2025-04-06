package com.nagarro.amcart.repository.elasticsearch;

import com.nagarro.amcart.model.elasticsearch.ESProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ESProductRepository extends ElasticsearchRepository<ESProduct, String>, ESProductRepositoryCustom {
    
    List<ESProduct> findByNameContainingOrDescriptionContaining(String name, String description);
    
    @Query("{\"bool\": {\"should\": [{\"match\": {\"name\": \"?0\"}}, {\"match\": {\"description\": \"?0\"}}]}}")
    List<ESProduct> search(String keyword);
    
    // Basic filters
    Page<ESProduct> findByCategoryId(String categoryId, Pageable pageable);
    
    Page<ESProduct> findByBrandKeyword(String brand, Pageable pageable);
    
    Page<ESProduct> findByColorKeyword(String color, Pageable pageable);
    
    Page<ESProduct> findByOnSaleIsTrue(Pageable pageable);
    
    Page<ESProduct> findByInStockIsTrue(Pageable pageable);
    
    // Combined filters
    Page<ESProduct> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    Page<ESProduct> findByAverageRatingGreaterThanEqual(double minRating, Pageable pageable);
    
    // We'll implement complex searches with multiple filters in the custom repository
}