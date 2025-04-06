package com.nagarro.amcart.repository.elasticsearch;

import com.nagarro.amcart.dto.request.SearchCriteriaRequest;
import com.nagarro.amcart.dto.response.SearchResponse;
import com.nagarro.amcart.model.elasticsearch.ESProduct;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ESProductRepositoryCustomImpl implements ESProductRepositoryCustom {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public Page<ESProduct> searchProducts(SearchCriteriaRequest criteria) {
        Query searchQuery = buildAdvancedSearchQuery(criteria);
        
        SearchHits<ESProduct> searchHits = elasticsearchOperations.search(searchQuery, ESProduct.class);
        
        List<ESProduct> products = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        
        return new PageImpl<>(
                products,
                PageRequest.of(criteria.getPage(), criteria.getSize()),
                searchHits.getTotalHits()
        );
    }

    @Override
    public SearchResponse searchProductsWithoutFacets(SearchCriteriaRequest criteria) {
        Query searchQuery = buildAdvancedSearchQuery(criteria);
        
        SearchHits<ESProduct> searchHits = elasticsearchOperations.search(searchQuery, ESProduct.class);
        
        List<ESProduct> products = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        
        int totalPages = (int) Math.ceil((double) searchHits.getTotalHits() / criteria.getSize());
        
        return SearchResponse.builder()
                .products(null) // Will be set in service layer
                .totalItems(searchHits.getTotalHits())
                .totalPages(totalPages)
                .currentPage(criteria.getPage())
                .build();
    }
    
    private Query buildAdvancedSearchQuery(SearchCriteriaRequest criteria) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        
        // Add keyword search with enhanced functionality
        if (StringUtils.hasText(criteria.getKeyword())) {
            String keyword = criteria.getKeyword().trim();
            
            // Main multi-match query for the keyword
            BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
            
            // High-priority exact matches in name and nameKeyword
            keywordQuery.should(QueryBuilders.matchPhraseQuery("name", keyword).boost(3.0f));
            keywordQuery.should(QueryBuilders.termQuery("nameKeyword", keyword).boost(3.0f));
            
            // Multi-field matches with different weights
            MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(keyword)
                    .field("name", 2.5f)
                    .field("nameKeyword", 2.5f)
                    .field("description", 1.5f)
                    .field("brand", 1.2f)
                    .field("brandKeyword", 1.2f)
                    .field("categoryName", 1.0f)
                    .field("categoryNameKeyword", 1.0f)
                    .field("color", 0.8f)
                    .field("colorKeyword", 0.8f)
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                    .minimumShouldMatch("2<-25%")
                    .fuzziness(Fuzziness.AUTO);
            
            keywordQuery.should(multiMatchQuery);
            
            // Fuzzy searches for typo tolerance
            keywordQuery.should(QueryBuilders.fuzzyQuery("name", keyword).boost(1.0f));
            keywordQuery.should(QueryBuilders.fuzzyQuery("brand", keyword).boost(0.8f));
            keywordQuery.should(QueryBuilders.fuzzyQuery("description", keyword).boost(0.7f));
            
            // Prefix query for partial matches
            keywordQuery.should(QueryBuilders.prefixQuery("name", keyword).boost(1.5f));
            keywordQuery.should(QueryBuilders.prefixQuery("brand", keyword).boost(1.0f));
            
            // Wildcards for more flexible matching
            keywordQuery.should(QueryBuilders.wildcardQuery("name", "*" + keyword.toLowerCase() + "*").boost(1.0f));
            
            // For multi-word queries, add token-based matching
            if (keyword.contains(" ")) {
                String[] tokens = keyword.split("\\s+");
                for (String token : tokens) {
                    if (token.length() > 2) {  // Ignore very short tokens
                        keywordQuery.should(QueryBuilders.multiMatchQuery(token)
                                .field("name", 1.0f)
                                .field("description", 0.8f)
                                .field("brand", 0.7f)
                                .fuzziness(Fuzziness.AUTO));
                    }
                }
            }
            
            boolQueryBuilder.must(keywordQuery);
        }
        
        // Add category filter
        if (StringUtils.hasText(criteria.getCategoryId())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId", criteria.getCategoryId()));
        }
        
        // Add price filter
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price")
                    .from(criteria.getMinPrice())
                    .to(criteria.getMaxPrice()));
        } else if (criteria.getMinPrice() != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price")
                    .from(criteria.getMinPrice()));
        } else if (criteria.getMaxPrice() != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price")
                    .to(criteria.getMaxPrice()));
        }
        
        // Create the query string for the combined query
        String queryString = boolQueryBuilder.toString();
        
        // Create the query with pagination
        return new StringQuery(queryString)
                .setPageable(PageRequest.of(criteria.getPage(), criteria.getSize()));
    }
}