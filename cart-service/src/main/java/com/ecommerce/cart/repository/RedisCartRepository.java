package com.ecommerce.cart.repository;

import com.ecommerce.cart.model.CartItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisCartRepository implements CartRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public CartItem findItem(Long userId, Long productId) {
        return (CartItem) redisTemplate.opsForHash().get(key(userId), productId.toString());
    }

    @Override
    public void saveItem(Long userId, CartItem item) {
        redisTemplate.opsForHash().put(key(userId), item.getProductId().toString(), item);
    }

    @Override
    public void removeItem(Long userId, Long productId) {
        redisTemplate.opsForHash().delete(key(userId), productId.toString());
    }

    @Override
    public List<CartItem> findItems(Long userId) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        return hashOperations.values(key(userId)).stream().map(value -> (CartItem) value).toList();
    }

    private String key(Long userId) {
        return "cart:" + userId;
    }
}
