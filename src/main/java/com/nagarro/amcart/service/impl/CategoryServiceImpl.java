package com.nagarro.amcart.service.impl;

import com.nagarro.amcart.dto.response.CategoryResponse;
import com.nagarro.amcart.exception.ResourceNotFoundException;
import com.nagarro.amcart.model.Category;
import com.nagarro.amcart.repository.CategoryRepository;
import com.nagarro.amcart.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @Override
    public List<Category> getSubcategories(String parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    @Override
    public List<Category> getCategoriesByGender(String gender) {
        return categoryRepository.findByGender(gender);
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", name));
    }

    @Override
    public List<CategoryResponse> getCategoryTree() {
        // Fetch all categories
        List<Category> allCategories = categoryRepository.findAll();
        
        // Group categories by level, starting with root categories (level 0)
        List<CategoryResponse> rootCategories = buildCategoryTreeRecursive(allCategories, null);
        
        return rootCategories;
    }

    @Override
    public List<CategoryResponse> getCategoryTreeByGender(String gender) {
        // Fetch all categories
        List<Category> allCategories = categoryRepository.findAll();
        
        // Filter root categories by gender
        List<Category> rootCategoriesByGender = allCategories.stream()
                .filter(category -> category.getParentId() == null)
                .filter(category -> gender.equals(category.getGender()) || category.getGender() == null)
                .collect(Collectors.toList());
        
        // Build the tree using those root categories
        List<CategoryResponse> categoryTree = rootCategoriesByGender.stream()
                .map(rootCategory -> {
                    CategoryResponse response = mapCategoryToResponse(rootCategory);
                    response.setChildren(getChildrenRecursive(allCategories, rootCategory.getId()));
                    return response;
                })
                .collect(Collectors.toList());
        
        return categoryTree;
    }
    
    private List<CategoryResponse> buildCategoryTreeRecursive(List<Category> allCategories, String parentId) {
        return allCategories.stream()
                .filter(category -> {
                    if (parentId == null) {
                        return category.getParentId() == null;
                    } else {
                        return parentId.equals(category.getParentId());
                    }
                })
                .map(category -> {
                    CategoryResponse response = mapCategoryToResponse(category);
                    response.setChildren(buildCategoryTreeRecursive(allCategories, category.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    private List<CategoryResponse> getChildrenRecursive(List<Category> allCategories, String parentId) {
        return allCategories.stream()
                .filter(category -> parentId.equals(category.getParentId()))
                .map(category -> {
                    CategoryResponse response = mapCategoryToResponse(category);
                    response.setChildren(getChildrenRecursive(allCategories, category.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    private CategoryResponse mapCategoryToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .level(category.getLevel())
                .gender(category.getGender())
                .children(new ArrayList<>())
                .build();
    }
}