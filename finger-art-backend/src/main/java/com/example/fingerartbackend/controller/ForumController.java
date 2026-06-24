package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.LikeToggleResult;
import com.example.fingerartbackend.entity.ForumPost;
import com.example.fingerartbackend.entity.ForumReply;
import com.example.fingerartbackend.service.AdminAuditService;
import com.example.fingerartbackend.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 社区论坛控制器。
 * 管理帖子的发布、浏览、点赞、回复及管理员审核，对应社区互动模块。
 */
@RestController
@RequestMapping("/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private AdminAuditService adminAuditService;

    /**
     * 查询论坛帖子列表。
     *
     * @param sort 排序方式，默认 latest
     * @return 帖子列表
     */
    @GetMapping("/posts")
    public Result<List<ForumPost>> listPosts(@RequestParam(defaultValue = "latest") String sort) {
        try {
            return Result.success(forumService.listPosts(sort, AuthContext.getUserId()));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询当前登录用户发布的帖子。
     *
     * @return 我的帖子列表
     */
    @GetMapping("/my-posts")
    public Result<List<ForumPost>> listMyPosts() {
        try {
            Long userId = AuthContext.getUserId();
            if (userId == null) {
                return Result.error(401, "请先登录");
            }
            return Result.success(forumService.listMyPosts(userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取帖子详情（含点赞状态等）。
     *
     * @param id 帖子 ID
     * @return 帖子详情 Map
     */
    @GetMapping("/posts/{id}")
    public Result<Map<String, Object>> getPost(@PathVariable Long id) {
        try {
            Long viewerId = AuthContext.getUserId();
            return Result.success(forumService.getPostDetail(id, viewerId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发布新帖子。
     *
     * @param body 含 title、content、imageUrl、authorId 的请求体
     * @return 创建的帖子
     */
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

    /**
     * 删除帖子（作者或管理员）。
     *
     * @param id 帖子 ID
     * @return 删除成功提示
     */
    @DeleteMapping("/posts/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        try {
            forumService.deletePost(id, AuthContext.getUserId());
            if (AuthContext.isAdmin()) {
                adminAuditService.log("DELETE_FORUM_POST", "FORUM", id, "删除论坛帖子 #" + id);
            }
            return Result.success("已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询帖子下的回复列表。
     *
     * @param id 帖子 ID
     * @return 回复列表
     */
    @GetMapping("/posts/{id}/replies")
    public Result<List<ForumReply>> listReplies(@PathVariable Long id) {
        try {
            return Result.success(forumService.listReplies(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 在帖子下发表回复。
     *
     * @param id   帖子 ID
     * @param body 含 content、authorId 的请求体
     * @return 创建的回复
     */
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

    /**
     * 切换帖子点赞状态。
     *
     * @param id 帖子 ID
     * @return 点赞切换结果（是否已点赞、点赞数）
     */
    @PostMapping("/posts/{id}/like")
    public Result<LikeToggleResult> likePost(@PathVariable Long id) {
        try {
            return Result.success(forumService.toggleLikePost(id, AuthContext.getUserId()));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员查询帖子列表，支持状态与关键词筛选。
     *
     * @param status  可选帖子状态
     * @param keyword 可选搜索关键词
     * @return 帖子列表
     */
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

    /**
     * 管理员获取帖子详情。
     *
     * @param id 帖子 ID
     * @return 帖子详情 Map
     */
    @GetMapping("/admin/posts/{id}")
    public Result<Map<String, Object>> adminGetPost(@PathVariable Long id) {
        try {
            return Result.success(forumService.getPostDetailForAdmin(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除回复（作者或管理员）。
     *
     * @param id 回复 ID
     * @return 删除成功提示
     */
    @DeleteMapping("/replies/{id}")
    public Result<String> deleteReply(@PathVariable Long id) {
        try {
            forumService.deleteReply(id, AuthContext.getUserId());
            return Result.success("已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员删除回复并记录审计日志。
     *
     * @param id 回复 ID
     * @return 删除成功提示
     */
    @DeleteMapping("/admin/replies/{id}")
    public Result<String> adminDeleteReply(@PathVariable Long id) {
        try {
            forumService.deleteReply(id, AuthContext.getUserId());
            adminAuditService.log("DELETE_FORUM_REPLY", "FORUM", id, "删除论坛回复 #" + id);
            return Result.success("已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员恢复已下架的帖子。
     *
     * @param id 帖子 ID
     * @return 恢复成功提示
     */
    @PostMapping("/admin/posts/{id}/restore")
    public Result<String> adminRestorePost(@PathVariable Long id) {
        try {
            forumService.restorePost(id);
            adminAuditService.log("RESTORE_FORUM_POST", "FORUM", id, "重新上架论坛帖子 #" + id);
            return Result.success("已重新上架");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
