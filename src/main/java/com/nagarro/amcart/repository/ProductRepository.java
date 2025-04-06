package com.nagarro.amcart.repository;

import com.nagarro.amcart.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    
    List<Product> findByCategoryId(String categoryId);
    
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("{'description': {$regex: ?0, $options: 'i'}}")
    List<Product> findByDescriptionContainingIgnoreCase(String description);
    
    @Query("{'$or': [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}")
    List<Product> searchProducts(String keyword);
    
    List<Product> findByInStockTrue();
    
    List<Product> findByOnSaleTrue();
    
    List<Product> findByBrand(String brand);
    
    List<Product> findByColor(String color);
}