package com.example.fingerartbackend.constant;

/**
 * 点赞/收藏目标类型常量。
 * <p>
 * 用于 {@code UserLike} 实体的 {@code targetType} 字段，区分不同业务对象的互动行为。
 * </p>
 */
public final class LikeTargetType {

    /** 作品点赞 */
    public static final String PRODUCT = "PRODUCT";

    /** 作品收藏（与点赞独立，不影响 likes 计数） */
    public static final String PRODUCT_FAVORITE = "PRODUCT_FAVORITE";

    /** 论坛帖子点赞 */
    public static final String FORUM_POST = "FORUM_POST";

    private LikeTargetType() {
    }
}
