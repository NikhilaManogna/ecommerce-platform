package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.AddCartItemRequest;
import com.ecommerce.cart.dto.CartItemResponse;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.UpdateCartItemRequest;
import com.ecommerce.cart.exception.ApiException;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        CartItem existing = cartRepository.findItem(userId, request.productId());
        int quantity = request.quantity() + (existing != null ? existing.getQuantity() : 0);
        cartRepository.saveItem(userId, new CartItem(request.productId(), request.productName(), request.price(), quantity));
        log.info("Added product {} to cart for user {}", request.productId(), userId);
        return getCart(userId);
    }

    public CartResponse updateItem(Long userId, Long productId, UpdateCartItemRequest request) {
        CartItem item = cartRepository.findItem(userId, productId);
        if (item == null) {
            throw new ApiException("Cart item not found");
        }
        item.setQuantity(request.quantity());
        cartRepository.saveItem(userId, item);
        return getCart(userId);
    }

    public void removeItem(Long userId, Long productId) {
        cartRepository.removeItem(userId, productId);
    }

    public CartResponse getCart(Long userId) {
        List<CartItem> storedItems = cartRepository.findItems(userId);
        List<CartItemResponse> items = new java.util.ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : storedItems) {
            BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);
            items.add(new CartItemResponse(item.getProductId(), item.getProductName(), item.getPrice(), item.getQuantity(), lineTotal));
        }
        return new CartResponse(userId, items, total);
    }
}
