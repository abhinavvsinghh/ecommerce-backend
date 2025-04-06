package com.nagarro.amcart.controller;

import com.nagarro.amcart.dto.response.CategoryResponse;
import com.nagarro.amcart.model.Category;
import com.nagarro.amcart.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable String id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/subcategories/{parentId}")
    public ResponseEntity<List<Category>> getSubcategories(@PathVariable String parentId) {
        List<Category> subcategories = categoryService.getSubcategories(parentId);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/gender/{gender}")
    public ResponseEntity<List<Category>> getCategoriesByGender(@PathVariable String gender) {
        List<Category> categories = categoryService.getCategoriesByGender(gender);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryResponse>> getCategoryTree() {
        List<CategoryResponse> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(categoryTree);
    }
    
    @GetMapping("/tree/gender/{gender}")
    public ResponseEntity<List<CategoryResponse>> getCategoryTreeByGender(@PathVariable String gender) {
        List<CategoryResponse> categoryTree = categoryService.getCategoryTreeByGender(gender);
        return ResponseEntity.ok(categoryTree);
    }
}