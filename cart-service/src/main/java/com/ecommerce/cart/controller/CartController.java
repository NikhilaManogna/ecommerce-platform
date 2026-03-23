package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.AddCartItemRequest;
import com.ecommerce.cart.dto.ApiResponse;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.UpdateCartItemRequest;
import com.ecommerce.cart.service.CartService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final com.ecommerce.cart.security.JwtService jwtService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                             @Valid @RequestBody AddCartItemRequest request) {
        CartResponse response = cartService.addItem(extractUserId(authorizationHeader), request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Cart updated successfully", response));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                                @PathVariable Long productId,
                                                                @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse response = cartService.updateItem(extractUserId(authorizationHeader), productId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Cart item updated successfully", response));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                        @PathVariable Long productId) {
        cartService.removeItem(extractUserId(authorizationHeader), productId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Cart item removed successfully", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        CartResponse response = cartService.getCart(extractUserId(authorizationHeader));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Cart fetched successfully", response));
    }

    private Long extractUserId(String header) {
        Claims claims = jwtService.validateToken(header.replace("Bearer ", ""));
        Integer integerId = claims.get("userId", Integer.class);
        if (integerId != null) {
            return integerId.longValue();
        }
        return claims.get("userId", Long.class);
    }
}
