package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.LikeToggleResult;
import com.example.fingerartbackend.entity.ForumPost;
import com.example.fingerartbackend.entity.ForumReply;
import com.example.fingerartbackend.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @GetMapping("/posts")
    public Result<List<ForumPost>> listPosts(@RequestParam(defaultValue = "latest") String sort) {
        try {
            return Result.success(forumService.listPosts(sort, AuthContext.getUserId()));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/posts/{id}")
    public Result<Map<String, Object>> getPost(@PathVariable Long id) {
        try {
            Long viewerId = AuthContext.getUserId();
            return Result.success(forumService.getPostDetail(id, viewerId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/posts")
    public Result<ForumPost> createPost(@RequestBody Map<String, Object> body) {
        try {
            Long authorId = AuthContext.getUserId();
            if (authorId == null && body.get("authorId") != null) {
                authorId = Long.valueOf(body.get("authorId").toString());
            }
            String title = body.get("title") != null ? body.get("title").toString() : null;
            String content = body.get("content") != null ? body.get("content").toString() : null;
            String imageUrl = body.get("imageUrl") != null ? body.get("imageUrl").toString() : null;
            return Result.success(forumService.createPost(authorId, title, content, imageUrl));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/posts/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        try {
            forumService.deletePost(id, AuthContext.getUserId());
            return Result.success("已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/posts/{id}/replies")
    public Result<List<ForumReply>> listReplies(@PathVariable Long id) {
        try {
            return Result.success(forumService.listReplies(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/posts/{id}/replies")
    public Result<ForumReply> createReply(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long authorId = AuthContext.getUserId();
            if (authorId == null && body.get("authorId") != null) {
                authorId = Long.valueOf(body.get("authorId").toString());
            }
            String content = body.get("content") != null ? body.get("content").toString() : null;
            return Result.success(forumService.createReply(id, authorId, content));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/posts/{id}/like")
    public Result<LikeToggleResult> likePost(@PathVariable Long id) {
        try {
            return Result.success(forumService.toggleLikePost(id, AuthContext.getUserId()));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admin/posts")
    public Result<List<ForumPost>> adminListPosts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        try {
            return Result.success(forumService.listPostsForAdmin(status, keyword));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admin/posts/{id}")
    public Result<Map<String, Object>> adminGetPost(@PathVariable Long id) {
        try {
            return Result.success(forumService.getPostDetailForAdmin(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/admin/replies/{id}")
    public Result<String> adminDeleteReply(@PathVariable Long id) {
        try {
            forumService.deleteReply(id, AuthContext.getUserId());
            return Result.success("已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
