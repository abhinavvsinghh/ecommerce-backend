package com.nagarro.amcart.service;

import com.nagarro.amcart.dto.response.CategoryResponse;
import com.nagarro.amcart.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();

    Category getCategoryById(String id);

    List<Category> getSubcategories(String parentId);

    List<Category> getCategoriesByGender(String gender);

    Category getCategoryByName(String name);

    List<CategoryResponse> getCategoryTree();

    List<CategoryResponse> getCategoryTreeByGender(String gender);
}