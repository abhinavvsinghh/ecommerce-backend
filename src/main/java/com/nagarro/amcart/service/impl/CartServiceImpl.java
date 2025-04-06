package com.nagarro.amcart.service.impl;

import com.nagarro.amcart.dto.request.AddToCartRequest;
import com.nagarro.amcart.dto.response.CartResponse;
import com.nagarro.amcart.exception.ResourceNotFoundException;
import com.nagarro.amcart.exception.UnauthorizedException;
import com.nagarro.amcart.model.Cart;
import com.nagarro.amcart.model.CartItem;
import com.nagarro.amcart.model.Product;
import com.nagarro.amcart.model.User;
import com.nagarro.amcart.repository.CartRepository;
import com.nagarro.amcart.repository.ProductRepository;
import com.nagarro.amcart.repository.UserRepository;
import com.nagarro.amcart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponse getCart() {
        String userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);
        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse addToCart(AddToCartRequest request) {
        String userId = getCurrentUserId();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        
        if (!product.isInStock() || product.getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Product is out of stock or not enough quantity available");
        }

        Cart cart = getOrCreateCart(userId);
        
        // Check if product already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update existing item
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setSubtotal(calculateSubtotal(product, item.getQuantity()));
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .image(product.getImages().isEmpty() ? null : product.getImages().get(0))
                    .quantity(request.getQuantity())
                    .price(calculateProductPrice(product))
                    .subtotal(calculateSubtotal(product, request.getQuantity()))
                    .size(request.getSize())
                    .color(request.getColor())
                    .build();
            
            cart.getItems().add(newItem);
        }
        
        // Recalculate cart totals
        updateCartTotals(cart);
        cart.setUpdatedAt(new Date());
        
        // Save updated cart
        Cart updatedCart = cartRepository.save(cart);
        return mapToCartResponse(updatedCart);
    }

    @Override
    public CartResponse updateCartItem(String productId, int quantity) {
        String userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);
        
        // Find the item to update
        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item", "productId", productId));
        
        // Get product details
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        if (quantity <= 0) {
            // Remove item if quantity is 0 or negative
            return removeFromCart(productId);
        } else if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Not enough stock available");
        }
        
        // Update quantity and subtotal
        itemToUpdate.setQuantity(quantity);
        itemToUpdate.setSubtotal(calculateSubtotal(product, quantity));
        
        // Recalculate cart totals
        updateCartTotals(cart);
        cart.setUpdatedAt(new Date());
        
        // Save updated cart
        Cart updatedCart = cartRepository.save(cart);
        return mapToCartResponse(updatedCart);
    }

    @Override
    public CartResponse removeFromCart(String productId) {
        String userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);
        
        // Remove item from cart
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        
        if (!removed) {
            throw new ResourceNotFoundException("Cart Item", "productId", productId);
        }
        
        // Recalculate cart totals
        updateCartTotals(cart);
        cart.setUpdatedAt(new Date());
        
        // Save updated cart
        Cart updatedCart = cartRepository.save(cart);
        return mapToCartResponse(updatedCart);
    }

    @Override
    public void clearCart() {
        String userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);
        
        cart.setItems(new ArrayList<>());
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setCouponCode(null);
        cart.setUpdatedAt(new Date());
        
        cartRepository.save(cart);
    }

    @Override
    public CartResponse applyCoupon(String couponCode) {
        // Simple coupon implementation - in a real app, you'd have a coupon service/repository
        String userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);
        
        // Apply a 10% discount for demonstration purposes
        cart.setCouponCode(couponCode);
        BigDecimal discount = cart.getTotalPrice().multiply(BigDecimal.valueOf(0.1)).setScale(2, RoundingMode.HALF_UP);
        cart.setDiscountAmount(discount);
        cart.setTotalPrice(cart.getTotalPrice().subtract(discount));
        cart.setUpdatedAt(new Date());
        
        Cart updatedCart = cartRepository.save(cart);
        return mapToCartResponse(updatedCart);
    }

    @Override
    public CartResponse removeCoupon() {
        String userId = getCurrentUserId();
        Cart cart = getOrCreateCart(userId);
        
        // Remove coupon and recalculate
        if (cart.getCouponCode() != null) {
            cart.setCouponCode(null);
            cart.setTotalPrice(cart.getTotalPrice().add(cart.getDiscountAmount()));
            cart.setDiscountAmount(BigDecimal.ZERO);
            cart.setUpdatedAt(new Date());
            
            Cart updatedCart = cartRepository.save(cart);
            return mapToCartResponse(updatedCart);
        }
        
        return mapToCartResponse(cart);
    }
    
    // Helper methods
    
    private Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .userId(userId)
                    .items(new ArrayList<>())
                    .totalPrice(BigDecimal.ZERO)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .discountAmount(BigDecimal.ZERO)
                    .build();
            return cartRepository.save(newCart);
        });
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        return user.getId();
    }
    
    private BigDecimal calculateProductPrice(Product product) {
        if (product.isOnSale() && product.getDiscountPercentage() != null) {
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    product.getDiscountPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            return product.getPrice().multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        }
        return product.getPrice();
    }
    
    private BigDecimal calculateSubtotal(Product product, int quantity) {
        return calculateProductPrice(product).multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
    
    private void updateCartTotals(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        cart.setTotalPrice(subtotal);
        
        // Reapply discount if coupon exists
        if (cart.getCouponCode() != null) {
            BigDecimal discount = subtotal.multiply(BigDecimal.valueOf(0.1)).setScale(2, RoundingMode.HALF_UP);
            cart.setDiscountAmount(discount);
            cart.setTotalPrice(subtotal.subtract(discount));
        }
    }
    
    private CartResponse mapToCartResponse(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(cart.getItems())
                .subtotal(cart.getItems().stream()
                        .map(CartItem::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .couponCode(cart.getCouponCode())
                .discountAmount(cart.getDiscountAmount())
                .totalPrice(cart.getTotalPrice())
                .itemCount(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum())
                .build();
    }
}