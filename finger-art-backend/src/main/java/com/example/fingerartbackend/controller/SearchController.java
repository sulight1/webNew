package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.SearchResultItem;
import com.example.fingerartbackend.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局搜索控制器。
 * 提供跨作品、用户、技能等内容的全站关键词搜索，对应搜索模块。
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 按关键词执行全站搜索。
     *
     * @param q     搜索关键词
     * @param limit 返回结果数量上限，默认 30
     * @return 搜索结果列表及总数
     */
    @GetMapping
    public Result<Map<String, Object>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "30") int limit) {
        List<SearchResultItem> items = searchService.search(q, limit);
        Map<String, Object> body = new HashMap<>();
        body.put("items", items);
        body.put("total", items.size());
        return Result.success(body);
    }
}
