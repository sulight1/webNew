package com.example.fingerartbackend.constant;

public final class LikeTargetType {
    public static final String PRODUCT = "PRODUCT";
    /** 作品收藏（与点赞独立，不影响 likes 计数） */
    public static final String PRODUCT_FAVORITE = "PRODUCT_FAVORITE";
    public static final String FORUM_POST = "FORUM_POST";

    private LikeTargetType() {
    }
}
