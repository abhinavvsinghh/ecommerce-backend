package com.nagarro.amcart.controller;

import com.nagarro.amcart.dto.request.AddToCartRequest;
import com.nagarro.amcart.dto.response.CartResponse;
import com.nagarro.amcart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        CartResponse cart = cartService.getCart();
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        CartResponse updatedCart = cartService.addToCart(request);
        return ResponseEntity.ok(updatedCart);
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable String productId,
            @RequestParam int quantity) {
        CartResponse updatedCart = cartService.updateCartItem(productId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable String productId) {
        CartResponse updatedCart = cartService.removeFromCart(productId);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCart() {
        cartService.clearCart();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cart cleared successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/coupon")
    public ResponseEntity<CartResponse> applyCoupon(@RequestParam String code) {
        CartResponse updatedCart = cartService.applyCoupon(code);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/coupon")
    public ResponseEntity<CartResponse> removeCoupon() {
        CartResponse updatedCart = cartService.removeCoupon();
        return ResponseEntity.ok(updatedCart);
    }
}