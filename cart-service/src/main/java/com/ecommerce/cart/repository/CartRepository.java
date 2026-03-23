package com.ecommerce.cart.repository;

import com.ecommerce.cart.model.CartItem;
import java.util.List;

public interface CartRepository {

    CartItem findItem(Long userId, Long productId);

    void saveItem(Long userId, CartItem item);

    void removeItem(Long userId, Long productId);

    List<CartItem> findItems(Long userId);
}
