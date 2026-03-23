package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.product.repository.ProductRepository;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String PRODUCT_LIST_KEY = "products::all";

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public ProductResponse create(ProductRequest request) {
        Product product = map(new Product(), request);
        Product saved = productRepository.save(product);
        invalidateCache();
        log.info("Created product id={}", saved.getId());
        return map(saved);
    }

    public List<ProductResponse> list() {
        Object cached = redisTemplate.opsForValue().get(PRODUCT_LIST_KEY);
        if (cached instanceof String cachedJson) {
            try {
                return objectMapper.readValue(cachedJson, new TypeReference<>() {
                });
            } catch (JsonProcessingException ex) {
                log.warn("Failed to parse cached product list, rebuilding cache", ex);
            }
        }
        List<ProductResponse> responses = productRepository.findAll().stream().map(this::map).toList();
        try {
            redisTemplate.opsForValue().set(PRODUCT_LIST_KEY, objectMapper.writeValueAsString(responses), Duration.ofMinutes(10));
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize product list cache", ex);
        }
        return responses;
    }

    public ProductResponse get(Long id) {
        return map(find(id));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product updated = map(find(id), request);
        Product saved = productRepository.save(updated);
        invalidateCache();
        return map(saved);
    }

    public void delete(Long id) {
        productRepository.delete(find(id));
        invalidateCache();
    }

    public ProductResponse updateInventory(Long id, Integer stock) {
        Product product = find(id);
        product.setStock(stock);
        Product saved = productRepository.save(product);
        invalidateCache();
        return map(saved);
    }

    private Product find(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ApiException("Product not found"));
    }

    private Product map(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(request.category());
        return product;
    }

    private ProductResponse map(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory());
    }

    private void invalidateCache() {
        redisTemplate.delete(PRODUCT_LIST_KEY);
    }
}
