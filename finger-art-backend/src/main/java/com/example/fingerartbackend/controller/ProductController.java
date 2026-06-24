package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.FavoriteToggleResult;
import com.example.fingerartbackend.dto.LikeToggleResult;
import com.example.fingerartbackend.entity.Product;
import com.example.fingerartbackend.service.AdminAuditService;
import com.example.fingerartbackend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作品（商品）控制器。
 * 负责作品的查询、发布、审核、库存、点赞收藏及相似推荐，对应市集与作品管理模块。
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private AdminAuditService adminAuditService;

    /**
     * 查询作品列表，支持多维度筛选。
     *
     * @param scope          查询范围，approved 仅返回已审核作品
     * @param type           作品类型筛选
     * @param category       分类筛选
     * @param craftTechnique 工艺技法筛选
     * @param creatorId      创作者 ID 筛选
     * @return 作品列表
     */
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

    /**
     * 按关键词搜索已审核作品。
     *
     * @param q     搜索关键词
     * @param limit 返回数量上限，默认 50
     * @return 匹配的作品列表
     */
    @GetMapping("/search")
    public Result<List<Product>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(productService.searchApprovedProducts(q, limit, AuthContext.getUserId()));
    }

    /**
     * 获取热门作品列表。
     *
     * @param limit 返回数量上限，默认 8
     * @return 热门作品列表
     */
    @GetMapping("/hot")
    public Result<List<Product>> getHotProducts(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(productService.getHotProducts(limit, AuthContext.getUserId()));
    }

    /**
     * 获取当前用户收藏的作品列表。
     *
     * @return 收藏作品列表
     */
    @GetMapping("/favorites")
    public Result<List<Product>> getFavoriteProducts() {
        try {
            Long userId = AuthContext.getUserId();
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return Result.success(productService.getFavoriteProducts(userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 初始化示例作品数据（开发/演示用）。
     *
     * @return 创建的示例作品
     */
    @GetMapping("/init")
    public Result<Product> initData() {
        return Result.success(productService.createInitialProduct());
    }

    /**
     * 按 ID 查询作品详情。
     *
     * @param id 作品 ID
     * @return 作品实体
     */
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id, AuthContext.getUserId()));
    }

    /**
     * 发布新作品。
     *
     * @param product 作品实体
     * @return 保存后的作品
     */
    @PostMapping
    public Result<Product> addProduct(@RequestBody Product product) {
        return Result.success(productService.saveProduct(product));
    }

    /**
     * 更新作品信息。
     *
     * @param id      作品 ID
     * @param product 更新后的作品数据
     * @return 更新后的作品
     */
    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return Result.success(productService.updateProduct(id, product, AuthContext.getUserId()));
    }

    /**
     * 更新作品库存数量。
     *
     * @param id    作品 ID
     * @param stock 新库存数量
     * @return 更新后的作品
     */
    @PatchMapping("/{id}/stock")
    public Result<Product> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        return Result.success(productService.updateStock(id, stock));
    }

    /**
     * 切换作品点赞状态。
     *
     * @param id 作品 ID
     * @return 点赞切换结果
     */
    @PostMapping("/{id}/like")
    public Result<LikeToggleResult> likeProduct(@PathVariable Long id) {
        return Result.success(productService.toggleLikeProduct(id, AuthContext.getUserId()));
    }

    /**
     * 切换作品收藏状态。
     *
     * @param id 作品 ID
     * @return 收藏切换结果
     */
    @PostMapping("/{id}/favorite")
    public Result<FavoriteToggleResult> favoriteProduct(@PathVariable Long id) {
        try {
            Long userId = AuthContext.getUserId();
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return Result.success(productService.toggleFavoriteProduct(id, userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员审核作品。
     *
     * @param id     作品 ID
     * @param status 审核结果状态
     * @return 审核后的作品
     */
    @PostMapping("/{id}/audit")
    public Result<Product> auditProduct(@PathVariable Long id, @RequestParam String status) {
        Product product = productService.auditProduct(id, status);
        adminAuditService.log("AUDIT_PRODUCT", "PRODUCT", id,
                "审核作品「" + product.getTitle() + "」为 " + status);
        return Result.success(product);
    }

    /**
     * 管理员批量审核作品。
     *
     * @param body 含 ids 列表与 status 的请求体
     * @return 成功审核的作品数量
     */
    @PostMapping("/batch-audit")
    public Result<Integer> batchAuditProducts(@RequestBody java.util.Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<Long> ids = ((java.util.List<?>) body.get("ids")).stream()
                .map(item -> Long.valueOf(item.toString()))
                .collect(java.util.stream.Collectors.toList());
        String status = body.get("status").toString();
        int count = productService.batchAuditProducts(ids, status);
        adminAuditService.log("BATCH_AUDIT_PRODUCT", "PRODUCT", null,
                "批量审核 " + count + " 件作品为 " + status + "，ID=" + ids);
        return Result.success(count);
    }

    /**
     * 删除指定作品。
     *
     * @param id 作品 ID
     * @return 删除成功提示
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id, AuthContext.getUserId());
        productService.deleteProduct(id);
        if (AuthContext.isAdmin()) {
            adminAuditService.log("DELETE_PRODUCT", "PRODUCT", id,
                    "删除作品「" + product.getTitle() + "」");
        }
        return Result.success("商品删除成功");
    }

    /**
     * 获取与指定作品相似的推荐列表。
     *
     * @param id    作品 ID
     * @param limit 返回数量上限，默认 6
     * @return 相似作品列表
     */
    @GetMapping("/{id}/similar")
    public Result<List<Product>> getSimilarProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "6") int limit) {
        return Result.success(productService.getSimilarProducts(id, limit, AuthContext.getUserId()));
    }
}
