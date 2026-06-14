package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.constant.LikeTargetType;
import com.example.fingerartbackend.dto.LikeToggleResult;
import com.example.fingerartbackend.entity.Product;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.ProductMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.LikeService;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.ProductService;
import com.example.fingerartbackend.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

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
    private LikeService likeService;

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

    private void populateLikedStatus(List<Product> products, Long viewerId) {
        if (viewerId == null || products.isEmpty()) {
            return;
        }
        List<Long> ids = products.stream().map(Product::getId).collect(Collectors.toList());
        Set<Long> likedIds = likeService.getLikedTargetIds(viewerId, LikeTargetType.PRODUCT, ids);
        products.forEach(p -> p.setLiked(likedIds.contains(p.getId())));
    }

    private void populateLikedStatus(Product product, Long viewerId) {
        if (viewerId == null || product == null) {
            return;
        }
        product.setLiked(likeService.isLiked(viewerId, LikeTargetType.PRODUCT, product.getId()));
    }

    @Override
    public List<Product> getAllProducts(Long viewerId) {
        List<Product> products = productMapper.findAll();
        products.forEach(this::populateCreatorAvatar);
        populateLikedStatus(products, viewerId);
        return products;
    }

    @Override
    public Product getProductById(Long id, Long viewerId) {
        Product product = productMapper.findById(id).orElseThrow(() -> new RuntimeException("商品不存在"));
        populateCreatorAvatar(product);
        populateLikedStatus(product, viewerId);
        return product;
    }

    private boolean hasAvailableStock(Product product) {
        int stock = product.getStock() != null ? product.getStock() : 1;
        return stock > 0;
    }

    private void ensureDefaultStock(Product product) {
        if (product.getStock() != null && product.getStock() > 0) return;
        if ("CUSTOMIZABLE".equals(product.getType())) {
            product.setStock(999);
        } else {
            product.setStock(1);
        }
    }

    @Override
    public List<Product> getApprovedProducts(Long viewerId) {
        List<Product> products = productMapper.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .filter(this::hasAvailableStock)
                .peek(this::populateCreatorAvatar)
                .peek(this::refreshBoost)
                .sorted(this::compareExposure)
                .collect(Collectors.toList());
        populateLikedStatus(products, viewerId);
        return products;
    }

    private void refreshBoost(Product p) {
        if (p.getBoostUntil() != null && p.getBoostUntil().isBefore(java.time.LocalDateTime.now())) {
            p.setExposureBoost(0);
            p.setBoostUntil(null);
        }
    }

    private int compareExposure(Product a, Product b) {
        int boostA = effectiveBoost(a);
        int boostB = effectiveBoost(b);
        if (boostA != boostB) return Integer.compare(boostB, boostA);
        int likesA = a.getLikes() != null ? a.getLikes() : 0;
        int likesB = b.getLikes() != null ? b.getLikes() : 0;
        return Integer.compare(likesB, likesA);
    }

    private int effectiveBoost(Product p) {
        refreshBoost(p);
        return p.getExposureBoost() != null ? p.getExposureBoost() : 0;
    }

    @Override
    public List<Product> getApprovedProductsByType(String type, Long viewerId) {
        List<Product> products = productMapper.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .filter(p -> type.equals(p.getType()))
                .filter(this::hasAvailableStock)
                .peek(this::populateCreatorAvatar)
                .collect(Collectors.toList());
        populateLikedStatus(products, viewerId);
        return products;
    }

    @Override
    public List<Product> getApprovedProductsByCategory(String category, Long viewerId) {
        List<Product> products = productMapper.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .filter(p -> category.equals(p.getCategory()))
                .filter(this::hasAvailableStock)
                .peek(this::populateCreatorAvatar)
                .collect(Collectors.toList());
        populateLikedStatus(products, viewerId);
        return products;
    }

    @Override
    public List<Product> getApprovedProductsByCraftTechnique(String craftTechnique, Long viewerId) {
        List<Product> products = productMapper.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .filter(p -> craftTechnique.equals(p.getCraftTechnique()))
                .filter(this::hasAvailableStock)
                .peek(this::populateCreatorAvatar)
                .collect(Collectors.toList());
        populateLikedStatus(products, viewerId);
        return products;
    }

    @Override
    public List<Product> getApprovedProductsByCreatorId(Long creatorId, Long viewerId) {
        List<Product> products = productMapper.findByCreatorId(creatorId).stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .peek(this::populateCreatorAvatar)
                .collect(Collectors.toList());
        populateLikedStatus(products, viewerId);
        return products;
    }

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

    private void ensureArtisanCreator(Product product) {
        Long creatorId = product.getCreatorId();
        if (creatorId == null && product.getCreator() != null) {
            creatorId = userMapper.findByUsername(product.getCreator()).map(User::getId).orElse(null);
        }
        ensureArtisanCreator(creatorId);
    }

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

    @Override
    public Product saveProduct(Product product) {
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

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public Product auditProduct(Long id, String status) {
        Product product = productMapper.findById(id).orElseThrow(() -> new RuntimeException("商品不存在"));
        product.setStatus(status);
        Product saved = productMapper.save(product);
        populateCreatorAvatar(saved);
        notifyAuditResult(saved, status);
        return saved;
    }

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

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existing = productMapper.findById(id).orElseThrow(() -> new RuntimeException("商品不存在"));
        ensureArtisanCreator(existing.getCreatorId());
        boolean requiresReview = hasContentChanges(existing, product);
        existing.setTitle(product.getTitle());
        existing.setPrice(product.getPrice());
        existing.setType(product.getType());
        existing.setCategory(product.getCategory());
        existing.setCraftTechnique(product.getCraftTechnique());
        existing.setDescription(product.getDescription());
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

    @Override
    public List<Product> getHotProducts(int limit, Long viewerId) {
        int cap = limit > 0 ? limit : 8;
        return getApprovedProducts(viewerId).stream()
                .sorted(Comparator.comparingInt((Product p) -> p.getLikes() != null ? p.getLikes() : 0).reversed())
                .limit(cap)
                .collect(Collectors.toList());
    }

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

    private boolean matchesKeyword(Product p, String kw) {
        return contains(p.getTitle(), kw)
                || contains(p.getDescription(), kw)
                || contains(p.getCategory(), kw)
                || contains(p.getCraftTechnique(), kw)
                || contains(p.getCreator(), kw);
    }

    private boolean contains(String text, String kw) {
        return text != null && text.toLowerCase().contains(kw);
    }

    private boolean similarTo(Product base, Product other) {
        if (base.getCategory() != null && base.getCategory().equals(other.getCategory())) return true;
        if (base.getCraftTechnique() != null && base.getCraftTechnique().equals(other.getCraftTechnique())) return true;
        if (base.getCreatorId() != null && base.getCreatorId().equals(other.getCreatorId())) return true;
        return false;
    }

    private int similarityScore(Product base, Product other) {
        int score = 0;
        if (base.getCategory() != null && base.getCategory().equals(other.getCategory())) score += 3;
        if (base.getCraftTechnique() != null && base.getCraftTechnique().equals(other.getCraftTechnique())) score += 2;
        if (base.getCreatorId() != null && base.getCreatorId().equals(other.getCreatorId())) score += 1;
        score += other.getLikes() != null ? other.getLikes() : 0;
        return score;
    }

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
}