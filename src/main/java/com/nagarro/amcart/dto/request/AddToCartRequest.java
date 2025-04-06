package com.nagarro.amcart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddToCartRequest {
    
    @NotBlank(message = "Product ID is required")
    private String productId;
    
    @Min(value = 1, message = "Quantity should be at least 1")
    private int quantity;
    
    private String size;
    private String color;
}