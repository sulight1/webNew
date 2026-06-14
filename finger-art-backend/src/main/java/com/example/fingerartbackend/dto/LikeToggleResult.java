package com.example.fingerartbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeToggleResult {
    /** 操作后是否处于已点赞状态（true=已赞，false=已取消） */
    @JsonProperty("liked")
    private boolean liked;

    /** 当前点赞总数 */
    @JsonProperty("count")
    private int count;
}
