package com.nagarro.amcart.dto.response;

import com.nagarro.amcart.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String id;
    private String userId;
    private List<CartItem> items;
    private BigDecimal subtotal;
    private String couponCode;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private int itemCount;
}