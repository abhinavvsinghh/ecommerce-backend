package com.nagarro.amcart.model.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
@Setting(settingPath = "es-settings.json")
public class ESProduct {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "custom_analyzer", searchAnalyzer = "search_analyzer")
    private String name;
    
    @Field(type = FieldType.Keyword, normalizer = "keyword_normalizer")
    private String nameKeyword;
    
    @Field(type = FieldType.Text, analyzer = "custom_analyzer", searchAnalyzer = "search_analyzer")
    private String description;
    
    @Field(type = FieldType.Double)
    private BigDecimal price;
    
    @Field(type = FieldType.Text, analyzer = "synonym_analyzer")
    private String brand;
    
    @Field(type = FieldType.Keyword, normalizer = "keyword_normalizer")
    private String brandKeyword;
    
    @Field(type = FieldType.Text, analyzer = "custom_analyzer")
    private String color;
    
    @Field(type = FieldType.Keyword, normalizer = "keyword_normalizer")
    private String colorKeyword;
    
    @Field(type = FieldType.Keyword)
    private List<String> sizes;
    
    @Field(type = FieldType.Keyword)
    private String categoryId;
    
    @Field(type = FieldType.Text, analyzer = "synonym_analyzer")
    private String categoryName;
    
    @Field(type = FieldType.Keyword, normalizer = "keyword_normalizer")
    private String categoryNameKeyword;
    
    @Field(type = FieldType.Boolean)
    private boolean inStock;
    
    @Field(type = FieldType.Boolean)
    private boolean onSale;
    
    @Field(type = FieldType.Double)
    private BigDecimal discountPercentage;
    
    @Field(type = FieldType.Double)
    private double averageRating;
    
    @Field(type = FieldType.Integer)
    private int reviewCount;
    
    @Field(type = FieldType.Date)
    private Date createdAt;
    
    @Field(type = FieldType.Date)
    private Date updatedAt;
}