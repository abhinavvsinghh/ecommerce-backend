package com.nagarro.amcart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private String id;
    private String name;
    private String description;
    private String parentId;
    private int level;
    private String gender;
    private List<CategoryResponse> children = new ArrayList<>();
}
