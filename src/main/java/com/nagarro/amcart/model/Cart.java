package com.nagarro.amcart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class Cart {
    @Id
    private String id;
    
    private String userId;
    private List<CartItem> items;
    private BigDecimal totalPrice;
    private Date createdAt;
    private Date updatedAt;
    private String couponCode;
    private BigDecimal discountAmount;
}
