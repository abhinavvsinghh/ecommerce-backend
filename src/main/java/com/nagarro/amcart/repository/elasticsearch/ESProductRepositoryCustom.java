package com.nagarro.amcart.repository.elasticsearch;

import com.nagarro.amcart.dto.request.SearchCriteriaRequest;
import com.nagarro.amcart.dto.response.SearchResponse;
import com.nagarro.amcart.model.elasticsearch.ESProduct;
import org.springframework.data.domain.Page;

public interface ESProductRepositoryCustom {
    
    /**
     * Advanced search with category and price filters
     * 
     * @param criteria The search criteria
     * @return Page of matching ESProduct objects
     */
    Page<ESProduct> searchProducts(SearchCriteriaRequest criteria);
    
    /**
     * Advanced search returning simplified response
     * 
     * @param criteria The search criteria
     * @return SearchResponse containing products
     */
    SearchResponse searchProductsWithoutFacets(SearchCriteriaRequest criteria);
}