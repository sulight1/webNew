package com.example.fingerartbackend.dto;

import lombok.Data;

@Data
public class SearchResultItem {
    private String entityType;
    private Long id;
    private String title;
    private String subtitle;
    private String image;
    private Double price;
    private String linkUrl;

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

    public static SearchResultItem customRequest(Long id, String title, String subtitle) {
        SearchResultItem item = new SearchResultItem();
        item.setEntityType("CUSTOM_REQUEST");
        item.setId(id);
        item.setTitle(title);
        item.setSubtitle(subtitle);
        item.setLinkUrl("/custom-request-pool");
        return item;
    }

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
