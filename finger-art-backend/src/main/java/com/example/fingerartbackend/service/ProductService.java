package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.FavoriteToggleResult;
import com.example.fingerartbackend.dto.LikeToggleResult;
import com.example.fingerartbackend.entity.Product;
import java.util.List;

/**
 * 作品/商品服务接口，定义业务能力（业务服务接口）。
 */
public interface ProductService {
    List<Product> getAllProducts(Long viewerId);
    Product getProductById(Long id, Long viewerId);
    List<Product> getApprovedProducts(Long viewerId);
    List<Product> getApprovedProductsByType(String type, Long viewerId);
    List<Product> getApprovedProductsByCategory(String category, Long viewerId);
    List<Product> getApprovedProductsByCraftTechnique(String craftTechnique, Long viewerId);
    List<Product> getApprovedProductsByCreatorId(Long creatorId, Long viewerId);
    Product createInitialProduct();
    Product saveProduct(Product product);
    LikeToggleResult toggleLikeProduct(Long id, Long userId);

    FavoriteToggleResult toggleFavoriteProduct(Long id, Long userId);
    void deleteProduct(Long id);
    Product auditProduct(Long id, String status);
    int batchAuditProducts(List<Long> ids, String status);
    Product updateProduct(Long id, Product product, Long operatorUserId);
    Product updateStock(Long id, Integer stock);
    List<Product> searchApprovedProducts(String q, int limit, Long viewerId);
    List<Product> getHotProducts(int limit, Long viewerId);
    List<Product> getSimilarProducts(Long id, int limit, Long viewerId);

    List<Product> getFavoriteProducts(Long userId);
}