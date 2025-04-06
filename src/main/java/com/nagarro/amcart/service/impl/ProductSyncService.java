package com.nagarro.amcart.service.impl;

import com.nagarro.amcart.model.Product;
import com.nagarro.amcart.repository.ProductRepository;
import com.nagarro.amcart.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSyncService {

    private final ProductRepository productRepository;
    private final ProductService productService;
    
    /**
     * Sync all products to ElasticSearch when the application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void syncProductsToElasticSearch() {
        log.info("Starting to sync products to ElasticSearch");
        
        List<Product> allProducts = productRepository.findAll();
        int count = 0;
        
        for (Product product : allProducts) {
            try {
                productService.indexProductToElasticSearch(product);
                count++;
            } catch (Exception e) {
                log.error("Failed to index product: {}", product.getId(), e);
            }
        }
        
        log.info("Finished syncing {} products to ElasticSearch", count);
    }
}