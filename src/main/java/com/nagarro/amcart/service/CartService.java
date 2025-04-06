package com.nagarro.amcart.service;

import com.nagarro.amcart.dto.request.AddToCartRequest;
import com.nagarro.amcart.dto.response.CartResponse;

public interface CartService {
    
    CartResponse getCart();
    
    CartResponse addToCart(AddToCartRequest request);
    
    CartResponse updateCartItem(String productId, int quantity);
    
    CartResponse removeFromCart(String productId);
    
    void clearCart();
    
    CartResponse applyCoupon(String couponCode);
    
    CartResponse removeCoupon();
}