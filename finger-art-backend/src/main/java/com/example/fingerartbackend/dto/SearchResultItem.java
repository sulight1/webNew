package com.example.fingerartbackend.dto;

import lombok.Data;

/**
 * 全局搜索结果项 DTO。
 * 统一封装作品、定制需求、技能等不同类型搜索命中结果。
 */
@Data
public class SearchResultItem {
    /** 实体类型：PRODUCT / CUSTOM_REQUEST / SKILL */
    private String entityType;

    /** 实体 ID */
    private Long id;

    /** 主标题 */
    private String title;

    /** 副标题/摘要 */
    private String subtitle;

    /** 封面图 URL */
    private String image;

    /** 价格（作品类有效） */
    private Double price;

    /** 前端跳转路径 */
    private String linkUrl;

    /** 构建作品搜索结果项 */
    public static SearchResultItem product(Long id, String title, String subtitle, String image, Double price) {
        SearchResultItem item = new SearchResultItem();
        item.setEntityType("PRODUCT");
        item.setId(id);
        item.setTitle(title);
        item.setSubtitle(subtitle);
        item.setImage(image);
        item.setPrice(price);
        item.setLinkUrl("/product/" + id);
        return item;
    }

    /** 构建定制需求搜索结果项 */
    public static SearchResultItem customRequest(Long id, String title, String subtitle) {
        SearchResultItem item = new SearchResultItem();
        item.setEntityType("CUSTOM_REQUEST");
        item.setId(id);
        item.setTitle(title);
        item.setSubtitle(subtitle);
        item.setLinkUrl("/custom-request-pool");
        return item;
    }

    /** 构建技能搜索结果项 */
    public static SearchResultItem skill(Long id, String title, String subtitle) {
        SearchResultItem item = new SearchResultItem();
        item.setEntityType("SKILL");
        item.setId(id);
        item.setTitle(title);
        item.setSubtitle(subtitle);
        item.setLinkUrl("/skill-exchange");
        return item;
    }
}
