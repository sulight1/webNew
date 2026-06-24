package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.constant.LikeTargetType;
import com.example.fingerartbackend.dto.FavoriteToggleResult;
import com.example.fingerartbackend.dto.LikeToggleResult;
import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.entity.Product;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.entity.UserLike;
import com.example.fingerartbackend.mapper.ProductMapper;
import com.example.fingerartbackend.mapper.UserLikeMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.LikeService;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.ProductService;
import com.example.fingerartbackend.service.SensitiveWordService;
import com.example.fingerartbackend.service.UserPunishmentService;
import com.example.fingerartbackend.constant.UserPunishmentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 作品/商品服务实现类。
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private UserPunishmentService userPunishmentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserLikeMapper userLikeMapper;

    /**
     * 执行 populateCreatorAvatar 相关逻辑。
     */
    private void populateCreatorAvatar(Product product) {
        if (product.getCreatorId() != null) {
            userMapper.findById(product.getCreatorId())
                    .ifPresent(u -> product.setCreatorAvatar(u.getAvatar()));
        } else if (product.getCreator() != null) {
            Optional<User> user = userMapper.findByUsername(product.getCreator());
            user.ifPresent(u -> {
                product.setCreatorId(u.getId());
                product.setCreatorAvatar(u.getAvatar());
            });
        }
    }

    /**
     * 执行 populateEngagementStatus 相关逻辑。
     */
    private void populateEngagementStatus(List<Product> products, Long viewerId) {
        if (viewerId == null || products.isEmpty()) {
            return;
        }
        List<Long> ids = products.stream().map(Product::getId).collect(Collectors.toList());
        Set<Long> likedIds = likeService.getLikedTargetIds(viewerId, LikeTargetType.PRODUCT, ids);
        Set<Long> favoritedIds = likeService.getLikedTargetIds(viewerId, LikeTargetType.PRODUCT_FAVORITE, ids);
        products.forEach(p -> {
            p.setLiked(likedIds.contains(p.getId()));
            p.setFavorited(favoritedIds.contains(p.getId()));
        });
    }

    /**
     * 执行 populateEngagementStatus 相关逻辑。
     */
    private void populateEngagementStatus(Product product, Long viewerId) {
        if (viewerId == null || product == null) {
            return;
        }
        product.setLiked(likeService.isLiked(viewerId, LikeTargetType.PRODUCT, product.getId()));
        product.setFavorited(likeService.isLiked(viewerId, LikeTargetType.PRODUCT_FAVORITE, product.getId()));
    }

    /**
     * 查询作品/商品信息。
     */
    @Override
    public List<Product> getAllProducts(Long viewerId) {
        List<Product> products = productMapper.findAll();
        products.forEach(this::populateCreatorAvatar);
        populateEngagementStatus(products, viewerId);
        return products;
    }

    /**
     * 查询作品/商品信息。
     */
    @Override
    public Product getProductById(Long id, Long viewerId) {
        Product product = productMapper.findById(id).orElseThrow(() -> new RuntimeException("商品不存在"));
        populateCreatorAvatar(product);
        populateEngagementStatus(product, viewerId);
        return product;
    }

    /**
     * 判断是否包含/拥有。
     */
    private boolean hasAvailableStock(Product product) {
        int stock = product.getStock() != null ? product.getStock() : 1;
        return stock > 0;
    }

    /**
     * 执行 ensureDefaultStock 相关逻辑。
     */
    private void ensureDefaultStock(Product product) {
        if (product.getStock() != null && product.getStock() > 0) return;
        if ("CUSTOMIZABLE".equals(product.getType())) {
            product.setStock(999);
        } else {
            product.setStock(1);
        }
    }

    /**
     * 查询作品/商品信息。
     */
    @Override
    public List<Product> getApprovedProducts(Long viewerId) {
        List<Product> products = productMapper.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .filter(this::hasAvailableStock)
                .peek(this::populateCreatorAvatar)
                .peek(this::refreshBoost)
                .sorted(this::compareExposure)
                .collect(Collectors.toList());
        populateEngagementStatus(products, viewerId);
        return products;
    }

    /**
     * 执行 refreshBoost 相关逻辑。
     */
    private void refreshBoost(Product p) {
        if (p.getBoostUntil() != null && p.getBoostUntil().isBefore(java.time.LocalDateTime.now())) {
            p.setExposureBoost(0);
            p.setBoostUntil(null);
        }
    }

    /**
     * 执行 compareExposure 相关逻辑。
     */
    private int compareExposure(Product a, Product b) {
        int boostA = effectiveBoost(a);
        int boostB = effectiveBoost(b);
        if (boostA != boostB) return Integer.compare(boostB, boostA);
        int likesA = a.getLikes() != null ? a.getLikes() : 0;
        int likesB = b.getLikes() != null ? b.getLikes() : 0;
        return Integer.compare(likesB, likesA);
    }

    /**
     * 执行 effectiveBoost 相关逻辑。
     */
    private int effectiveBoost(Product p) {
        refreshBoost(p);
        return p.getExposureBoost() != null ? p.getExposureBoost() : 0;
    }

    /**
     * 查询作品/商品信息。
     */
    @Override
    public List<Product> getApprovedProductsByType(String type, Long viewerId) {
        List<Product> products = productMapper.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .filter(p -> type.equals(p.getType()))
                .filter(this::hasAvailableStock)
                .peek(this::populateCreatorAvatar)
                .collect(Collectors.toList());
        populateEngagementStatus(products, viewerId);
        return products;
    }

    /**
     * 查询作品/商品信息。
     */
    @Override
    public List<Product> getApprovedProductsByCategory(String category, Long viewerId) {
        List<Product> products = productMapper.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .filter(p -> category.equals(p.getCategory()))
                .filter(this::hasAvailableStock)
                .peek(this::populateCreatorAvatar)
                .collect(Collectors.toList());
        populateEngagementStatus(products, viewerId);
        return products;
    }

    /**
     * 查询作品/商品信息。
     */
    @Override
    public List<Product> getApprovedProductsByCraftTechnique(String craftTechnique, Long viewerId) {
        List<Product> products = productMapper.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .filter(p -> craftTechnique.equals(p.getCraftTechnique()))
                .filter(this::hasAvailableStock)
                .peek(this::populateCreatorAvatar)
                .collect(Collectors.toList());
        populateEngagementStatus(products, viewerId);
        return products;
    }

    /**
     * 查询作品/商品信息。
     */
    @Override
    public List<Product> getApprovedProductsByCreatorId(Long creatorId, Long viewerId) {
        List<Product> products = productMapper.findByCreatorId(creatorId).stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .peek(this::populateCreatorAvatar)
                .collect(Collectors.toList());
        populateEngagementStatus(products, viewerId);
        return products;
    }

    /**
     * 创建作品/商品。
     */
    @Override
    public Product createInitialProduct() {
        Product p = new Product();
        p.setTitle("数据库里的手工熊");
        p.setPrice(199.0);
        p.setType("READY_MADE");
        p.setCreator("数据库管理员");
        p.setLikes(0);
        p.setStatus("APPROVED"); // 初始数据直接通过
        Product saved = productMapper.save(p);
        populateCreatorAvatar(saved);
        return saved;
    }

    /**
     * 执行 resolveCreatorId 相关逻辑。
     */
    private Long resolveCreatorId(Product product) {
        if (product.getCreatorId() != null) {
            return product.getCreatorId();
        }
        if (product.getCreator() != null) {
            return userMapper.findByUsername(product.getCreator()).map(User::getId).orElse(null);
        }
        return null;
    }

    /** 旧数据可能只有 creator 用户名，补全 creatorId 便于后续校验与关联 */
    private void backfillCreatorId(Product product) {
        if (product.getCreatorId() != null) {
            return;
        }
        Long resolved = resolveCreatorId(product);
        if (resolved != null) {
            product.setCreatorId(resolved);
        }
    }

    /**
     * 执行 ensureArtisanCreator 相关逻辑。
     */
    private void ensureArtisanCreator(Product product) {
        Long creatorId = resolveCreatorId(product);
        ensureArtisanCreator(creatorId);
    }

    /**
     * 执行 ensureArtisanCreator 相关逻辑。
     */
    private void ensureArtisanCreator(Long creatorId) {
        if (creatorId == null) {
            throw new RuntimeException("缺少创作者信息");
        }
        User creator = userMapper.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("创作者不存在"));
        if (!"ARTISAN".equals(creator.getRole())) {
            throw new RuntimeException("仅认证手作达人可发布或修改作品");
        }
    }

    /**
     * 保存作品/商品。
     */
    @Override
    public Product saveProduct(Product product) {
        assertCanPublishProduct(product);
        if (product.getTitle() != null) {
            sensitiveWordService.validateText(product.getTitle(), "作品标题");
        }
        if (product.getDescription() != null) {
            sensitiveWordService.validateText(product.getDescription(), "作品描述");
        }
        ensureArtisanCreator(product);
        if (product.getLikes() == null) product.setLikes(0);
        if (product.getStatus() == null) product.setStatus("PENDING");
        ensureDefaultStock(product);
        
        // 确保设置了 creatorId
        if (product.getCreatorId() == null && product.getCreator() != null) {
            userMapper.findByUsername(product.getCreator())
                    .ifPresent(u -> product.setCreatorId(u.getId()));
        }
        
        Product saved = productMapper.save(product);
        populateCreatorAvatar(saved);
        return saved;
    }

    /**
     * 切换作品/商品状态。
     */
    @Override
    @Transactional
    public LikeToggleResult toggleLikeProduct(Long id, Long userId) {
        Product product = productMapper.findById(id).orElseThrow(() -> new RuntimeException("商品不存在"));
        boolean liked = likeService.toggle(userId, LikeTargetType.PRODUCT, id);
        int current = product.getLikes() != null ? product.getLikes() : 0;
        product.setLikes(liked ? current + 1 : Math.max(0, current - 1));
        productMapper.save(product);
        return new LikeToggleResult(liked, product.getLikes());
    }

    /**
     * 切换作品/商品状态。
     */
    @Override
    @Transactional
    public FavoriteToggleResult toggleFavoriteProduct(Long id, Long userId) {
        productMapper.findById(id).orElseThrow(() -> new RuntimeException("商品不存在"));
        boolean favorited = likeService.toggle(userId, LikeTargetType.PRODUCT_FAVORITE, id);
        return new FavoriteToggleResult(favorited);
    }

    /**
     * 删除作品/商品。
     */
    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }

    /**
     * 审核作品/商品。
     */
    @Override
    public Product auditProduct(Long id, String status) {
        Product product = productMapper.findById(id).orElseThrow(() -> new RuntimeException("商品不存在"));
        product.setStatus(status);
        Product saved = productMapper.save(product);
        populateCreatorAvatar(saved);
        notifyAuditResult(saved, status);
        return saved;
    }

    /**
     * 执行 batchAuditProducts 相关逻辑。
     */
    @Override
    public int batchAuditProducts(List<Long> ids, String status) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new RuntimeException("无效的审核状态");
        }
        int count = 0;
        for (Long id : ids) {
            java.util.Optional<Product> opt = productMapper.findById(id);
            if (opt.isPresent()) {
                Product product = opt.get();
                product.setStatus(status);
                productMapper.save(product);
                notifyAuditResult(product, status);
                count++;
            }
        }
        return count;
    }

    /**
     * 更新作品/商品。
     */
    @Override
    public Product updateProduct(Long id, Product product, Long operatorUserId) {
        Product existing = productMapper.findById(id).orElseThrow(() -> new RuntimeException("商品不存在"));
        backfillCreatorId(existing);
        assertCanPublishProduct(existing);
        ensureArtisanCreator(existing);
        assertProductOwner(existing, operatorUserId);

        boolean requiresReview = hasContentChanges(existing, product);
        if (product.getTitle() != null) {
            sensitiveWordService.validateText(product.getTitle(), "作品标题");
            existing.setTitle(product.getTitle());
        }
        if (product.getPrice() != null) {
            existing.setPrice(product.getPrice());
        }
        if (product.getType() != null) {
            existing.setType(product.getType());
        }
        if (product.getCategory() != null) {
            existing.setCategory(product.getCategory());
        }
        if (product.getCraftTechnique() != null) {
            existing.setCraftTechnique(product.getCraftTechnique());
        }
        if (product.getDescription() != null) {
            sensitiveWordService.validateText(product.getDescription(), "作品描述");
            existing.setDescription(product.getDescription());
        }
        if (product.getDetailImages() != null) {
            existing.setDetailImages(product.getDetailImages());
        }
        if (product.getProcessImages() != null) {
            existing.setProcessImages(product.getProcessImages());
        }
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            existing.setImage(product.getImage());
        }
        if (product.getStock() != null && product.getStock() >= 0) {
            existing.setStock(product.getStock());
        }
        if (requiresReview) {
            existing.setStatus("PENDING");
        }
        Product saved = productMapper.save(existing);
        populateCreatorAvatar(saved);
        return saved;
    }

    /**
     * 断言业务条件，不满足则抛异常。
     */
    private void assertProductOwner(Product existing, Long operatorUserId) {
        if (operatorUserId == null) {
            throw new RuntimeException("请先登录");
        }
        if (AuthContext.isAdmin()) {
            return;
        }
        Long ownerId = resolveCreatorId(existing);
        if (ownerId == null || !ownerId.equals(operatorUserId)) {
            throw new RuntimeException("无权修改该作品");
        }
    }

    /**
     * 更新作品/商品。
     */
    @Override
    public Product updateStock(Long id, Integer stock) {
        if (stock == null || stock < 0) {
            throw new RuntimeException("库存数量无效");
        }
        Product existing = productMapper.findById(id).orElseThrow(() -> new RuntimeException("商品不存在"));
        existing.setStock(stock);
        Product saved = productMapper.save(existing);
        populateCreatorAvatar(saved);
        return saved;
    }

    /**
     * 判断是否包含/拥有。
     */
    private boolean hasContentChanges(Product existing, Product incoming) {
        if (incoming.getTitle() != null && !incoming.getTitle().equals(existing.getTitle())) return true;
        if (incoming.getPrice() != null && !incoming.getPrice().equals(existing.getPrice())) return true;
        if (incoming.getType() != null && !incoming.getType().equals(existing.getType())) return true;
        if (incoming.getCategory() != null && !incoming.getCategory().equals(existing.getCategory())) return true;
        if (incoming.getCraftTechnique() != null
                && !java.util.Objects.equals(incoming.getCraftTechnique(), existing.getCraftTechnique())) return true;
        if (incoming.getDescription() != null
                && !java.util.Objects.equals(incoming.getDescription(), existing.getDescription())) return true;
        if (incoming.getImage() != null && !incoming.getImage().isEmpty()
                && !incoming.getImage().equals(existing.getImage())) return true;
        if (incoming.getDetailImages() != null
                && !java.util.Objects.equals(incoming.getDetailImages(), existing.getDetailImages())) return true;
        if (incoming.getProcessImages() != null
                && !java.util.Objects.equals(incoming.getProcessImages(), existing.getProcessImages())) return true;
        return false;
    }

    /**
     * 搜索作品/商品。
     */
    @Override
    public List<Product> searchApprovedProducts(String q, int limit, Long viewerId) {
        if (q == null || q.trim().isEmpty()) {
            return List.of();
        }
        String kw = q.trim().toLowerCase();
        int cap = limit > 0 ? limit : 50;
        return getApprovedProducts(viewerId).stream()
                .filter(p -> matchesKeyword(p, kw))
                .limit(cap)
                .collect(Collectors.toList());
    }

    /**
     * 查询作品/商品信息。
     */
    @Override
    public List<Product> getHotProducts(int limit, Long viewerId) {
        int cap = limit > 0 ? limit : 8;
        return getApprovedProducts(viewerId).stream()
                .sorted(Comparator.comparingInt((Product p) -> p.getLikes() != null ? p.getLikes() : 0).reversed())
                .limit(cap)
                .collect(Collectors.toList());
    }

    /**
     * 查询作品/商品信息。
     */
    @Override
    public List<Product> getSimilarProducts(Long id, int limit, Long viewerId) {
        Product base = getProductById(id, viewerId);
        int cap = limit > 0 ? limit : 6;
        return getApprovedProducts(viewerId).stream()
                .filter(p -> !p.getId().equals(id))
                .filter(p -> similarTo(base, p))
                .sorted(Comparator.comparingInt((Product p) -> similarityScore(base, p)).reversed())
                .limit(cap)
                .collect(Collectors.toList());
    }

    /**
     * 执行 matchesKeyword 相关逻辑。
     */
    private boolean matchesKeyword(Product p, String kw) {
        return contains(p.getTitle(), kw)
                || contains(p.getDescription(), kw)
                || contains(p.getCategory(), kw)
                || contains(p.getCraftTechnique(), kw)
                || contains(p.getCreator(), kw);
    }

    /**
     * 执行 contains 相关逻辑。
     */
    private boolean contains(String text, String kw) {
        return text != null && text.toLowerCase().contains(kw);
    }

    /**
     * 执行 similarTo 相关逻辑。
     */
    private boolean similarTo(Product base, Product other) {
        if (base.getCategory() != null && base.getCategory().equals(other.getCategory())) return true;
        if (base.getCraftTechnique() != null && base.getCraftTechnique().equals(other.getCraftTechnique())) return true;
        if (base.getCreatorId() != null && base.getCreatorId().equals(other.getCreatorId())) return true;
        return false;
    }

    /**
     * 执行 similarityScore 相关逻辑。
     */
    private int similarityScore(Product base, Product other) {
        int score = 0;
        if (base.getCategory() != null && base.getCategory().equals(other.getCategory())) score += 3;
        if (base.getCraftTechnique() != null && base.getCraftTechnique().equals(other.getCraftTechnique())) score += 2;
        if (base.getCreatorId() != null && base.getCreatorId().equals(other.getCreatorId())) score += 1;
        score += other.getLikes() != null ? other.getLikes() : 0;
        return score;
    }

    /**
     * 发送通知。
     */
    private void notifyAuditResult(Product product, String status) {
        if (product.getCreatorId() == null) return;
        boolean approved = "APPROVED".equals(status);
        String title = approved ? "作品审核通过" : "作品审核未通过";
        String content = approved
                ? "你的作品「" + product.getTitle() + "」已上架市集"
                : "你的作品「" + product.getTitle() + "」未通过审核，可修改后重新提交";
        notificationService.notify(
                product.getCreatorId(),
                "AUDIT",
                title,
                content,
                "/artisan-dashboard?menu=my-products");
    }

    /**
     * 查询作品/商品信息。
     */
    @Override
    public List<Product> getFavoriteProducts(Long userId) {
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        List<UserLike> likes = userLikeMapper.findByUserIdAndTargetTypeOrderByCreateTimeDesc(
                userId, LikeTargetType.PRODUCT_FAVORITE);
        if (likes.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = likes.stream().map(UserLike::getTargetId).collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<Product> result = new ArrayList<>();
        for (UserLike like : likes) {
            Product product = productMap.get(like.getTargetId());
            if (product != null) {
                result.add(product);
            }
        }
        result.forEach(this::populateCreatorAvatar);
        populateEngagementStatus(result, userId);
        return result;
    }

    /**
     * 断言业务条件，不满足则抛异常。
     */
    private void assertCanPublishProduct(Product product) {
        Long creatorId = resolveCreatorId(product);
        if (creatorId != null) {
            userPunishmentService.assertNotPunished(creatorId, UserPunishmentType.NO_PRODUCT, "您已被禁止上架商品");
        }
    }
}