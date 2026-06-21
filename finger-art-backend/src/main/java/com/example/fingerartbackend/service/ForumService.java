package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.LikeToggleResult;
import com.example.fingerartbackend.entity.ForumPost;
import com.example.fingerartbackend.entity.ForumReply;

import java.util.List;
import java.util.Map;

public interface ForumService {
    List<ForumPost> listPosts(String sort, Long viewerId);

    List<ForumPost> listMyPosts(Long authorId);

    ForumPost getPost(Long id, boolean incrementView);

    ForumPost createPost(Long authorId, String title, String content, String imageUrl);

    void deletePost(Long id, Long operatorId);

    List<ForumReply> listReplies(Long postId);

    ForumReply createReply(Long postId, Long authorId, String content);

    LikeToggleResult toggleLikePost(Long postId, Long userId);

    Map<String, Object> getPostDetail(Long id, Long viewerId);

    List<ForumPost> listPostsForAdmin(String status, String keyword);

    Map<String, Object> getPostDetailForAdmin(Long id);

    void deleteReply(Long id, Long operatorId);

    void restorePost(Long id);
}
