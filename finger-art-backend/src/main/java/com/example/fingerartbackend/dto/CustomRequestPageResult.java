package com.example.fingerartbackend.dto;

import com.example.fingerartbackend.entity.CustomRequest;
import lombok.Data;

import java.util.List;

/**
 * 定制需求分页结果 DTO。
 * 用于需求大厅列表接口的分页响应。
 */
@Data
public class CustomRequestPageResult {
    /** 当前页需求列表 */
    private List<CustomRequest> items;

    /** 符合条件的总条数 */
    private long total;

    /** 当前页码（从 0 或 1 起，与接口约定一致） */
    private int page;

    /** 每页条数 */
    private int size;
}
