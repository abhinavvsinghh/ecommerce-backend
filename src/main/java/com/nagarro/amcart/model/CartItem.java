package com.nagarro.amcart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String productId;
    private String productName;
    private String image;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private String size;
    private String color;
}