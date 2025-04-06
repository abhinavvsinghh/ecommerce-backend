package com.nagarro.amcart.repository;

import com.nagarro.amcart.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    
    List<Category> findByParentId(String parentId);
    
    List<Category> findByGender(String gender);
    
    Optional<Category> findByName(String name);
}