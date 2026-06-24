package com.example.fingerartbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏切换结果 DTO。
 * 用于作品收藏/取消收藏接口的响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteToggleResult {
    /** 操作后是否处于已收藏状态 */
    @JsonProperty("favorited")
    private boolean favorited;
}
