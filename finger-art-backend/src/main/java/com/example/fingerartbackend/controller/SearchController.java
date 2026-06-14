package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.SearchResultItem;
import com.example.fingerartbackend.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

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
