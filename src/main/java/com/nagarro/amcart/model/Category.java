package com.nagarro.amcart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
public class Category {
    @Id
    private String id;
    
    private String name;
    private String description;
    private String parentId;
    private int level; // Hierarchy level (0 for root, 1 for first level, etc.)
    private String gender; // "men", "women", or null for unisex
    private Date createdAt;
    private Date updatedAt;
    
    @Transient
    private List<Category> children = new ArrayList<>();
}