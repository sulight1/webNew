package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.LikeToggleResult;
import com.example.fingerartbackend.entity.Product;
import com.example.fingerartbackend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<List<Product>> getProducts(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String craftTechnique,
            @RequestParam(required = false) Long creatorId) {
        Long viewerId = AuthContext.getUserId();
        if ("approved".equals(scope)) {
            if (creatorId != null) {
                return Result.success(productService.getApprovedProductsByCreatorId(creatorId, viewerId));
            }
            if (craftTechnique != null && !craftTechnique.isEmpty()) {
                return Result.success(productService.getApprovedProductsByCraftTechnique(craftTechnique, viewerId));
            }
            if (category != null && !category.isEmpty()) {
                return Result.success(productService.getApprovedProductsByCategory(category, viewerId));
            }
            if (type != null && !type.isEmpty()) {
                return Result.success(productService.getApprovedProductsByType(type, viewerId));
            }
            return Result.success(productService.getApprovedProducts(viewerId));
        }
        return Result.success(productService.getAllProducts(viewerId));
    }

    @GetMapping("/search")
    public Result<List<Product>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(productService.searchApprovedProducts(q, limit, AuthContext.getUserId()));
    }

    @GetMapping("/hot")
    public Result<List<Product>> getHotProducts(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(productService.getHotProducts(limit, AuthContext.getUserId()));
    }

    @GetMapping("/init")
    public Result<Product> initData() {
        return Result.success(productService.createInitialProduct());
    }

    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id, AuthContext.getUserId()));
    }

    @PostMapping
    public Result<Product> addProduct(@RequestBody Product product) {
        return Result.success(productService.saveProduct(product));
    }

    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return Result.success(productService.updateProduct(id, product));
    }

    @PatchMapping("/{id}/stock")
    public Result<Product> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        return Result.success(productService.updateStock(id, stock));
    }

    @PostMapping("/{id}/like")
    public Result<LikeToggleResult> likeProduct(@PathVariable Long id) {
        return Result.success(productService.toggleLikeProduct(id, AuthContext.getUserId()));
    }

    @PostMapping("/{id}/audit")
    public Result<Product> auditProduct(@PathVariable Long id, @RequestParam String status) {
        return Result.success(productService.auditProduct(id, status));
    }

    @PostMapping("/batch-audit")
    public Result<Integer> batchAuditProducts(@RequestBody java.util.Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<Long> ids = ((java.util.List<?>) body.get("ids")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toList());
        String status = body.get("status").toString();
        return Result.success(productService.batchAuditProducts(ids, status));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("商品删除成功");
    }

    @GetMapping("/{id}/similar")
    public Result<List<Product>> getSimilarProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "6") int limit) {
        return Result.success(productService.getSimilarProducts(id, limit, AuthContext.getUserId()));
    }
}
