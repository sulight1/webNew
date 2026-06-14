package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.constant.LikeTargetType;
import com.example.fingerartbackend.dto.LikeToggleResult;
import com.example.fingerartbackend.entity.ForumPost;
import com.example.fingerartbackend.entity.ForumReply;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.ForumPostMapper;
import com.example.fingerartbackend.mapper.ForumReplyMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.ForumService;
import com.example.fingerartbackend.service.LikeService;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ForumServiceImpl implements ForumService {

    @Autowired
    private ForumPostMapper postMapper;

    @Autowired
    private ForumReplyMapper replyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LikeService likeService;

    private void populateLikedStatus(List<ForumPost> posts, Long viewerId) {
        if (viewerId == null || posts.isEmpty()) {
            return;
        }
        List<Long> ids = posts.stream().map(ForumPost::getId).collect(Collectors.toList());
        Set<Long> likedIds = likeService.getLikedTargetIds(viewerId, LikeTargetType.FORUM_POST, ids);
        posts.forEach(p -> p.setLiked(likedIds.contains(p.getId())));
    }

    @Override
    public List<ForumPost> listPosts(String sort, Long viewerId) {
        List<ForumPost> posts;
        if ("hot".equalsIgnoreCase(sort)) {
            posts = postMapper.findByStatusOrderByReplyCountDescCreateTimeDesc("ACTIVE");
        } else {
            posts = postMapper.findByStatusOrderByCreateTimeDesc("ACTIVE");
        }
        populateLikedStatus(posts, viewerId);
        return posts;
    }

    @Override
    @Transactional
    public ForumPost getPost(Long id, boolean incrementView) {
        ForumPost post = postMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        if (!"ACTIVE".equals(post.getStatus())) {
            throw new RuntimeException("帖子不可用");
        }
        if (incrementView) {
            post.setViewCount((post.getViewCount() != null ? post.getViewCount() : 0) + 1);
            postMapper.save(post);
        }
        return post;
    }

    @Override
    @Transactional
    public ForumPost createPost(Long authorId, String title, String content, String imageUrl) {
        if (authorId == null) {
            throw new RuntimeException("请先登录");
        }
        if (title == null || title.isBlank()) {
            throw new RuntimeException("标题不能为空");
        }
        if (content == null || content.isBlank()) {
            throw new RuntimeException("内容不能为空");
        }
        User author = userMapper.findById(authorId).orElseThrow(() -> new RuntimeException("用户不存在"));
        sensitiveWordService.validateText(title, "标题");
        sensitiveWordService.validateText(content, "内容");

        ForumPost post = new ForumPost();
        post.setAuthorId(authorId);
        post.setAuthorName(author.getUsername());
        post.setAuthorAvatar(author.getAvatar());
        post.setTitle(title.trim());
        post.setContent(content.trim());
        if (imageUrl != null && !imageUrl.isBlank()) {
            post.setImageUrl(imageUrl.trim());
        }
        return postMapper.save(post);
    }

    @Override
    @Transactional
    public void deletePost(Long id, Long operatorId) {
        ForumPost post = postMapper.findById(id).orElseThrow(() -> new RuntimeException("帖子不存在"));
        boolean isOwner = operatorId != null && operatorId.equals(post.getAuthorId());
        boolean isAdmin = AuthContext.isAdmin();
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("无权删除该帖子");
        }
        post.setStatus("REMOVED");
        postMapper.save(post);
    }

    @Override
    public List<ForumReply> listReplies(Long postId) {
        getPost(postId, false);
        return replyMapper.findByPostIdAndStatusOrderByCreateTimeAsc(postId, "ACTIVE");
    }

    @Override
    @Transactional
    public ForumReply createReply(Long postId, Long authorId, String content) {
        if (authorId == null) {
            throw new RuntimeException("请先登录");
        }
        if (content == null || content.isBlank()) {
            throw new RuntimeException("回复内容不能为空");
        }
        ForumPost post = getPost(postId, false);
        User author = userMapper.findById(authorId).orElseThrow(() -> new RuntimeException("用户不存在"));
        sensitiveWordService.validateText(content, "回复内容");

        ForumReply reply = new ForumReply();
        reply.setPostId(postId);
        reply.setAuthorId(authorId);
        reply.setAuthorName(author.getUsername());
        reply.setAuthorAvatar(author.getAvatar());
        reply.setContent(content.trim());
        ForumReply saved = replyMapper.save(reply);

        post.setReplyCount((int) replyMapper.countByPostIdAndStatus(postId, "ACTIVE"));
        postMapper.save(post);

        if (!authorId.equals(post.getAuthorId())) {
            String preview = content.length() > 40 ? content.substring(0, 40) + "..." : content;
            notificationService.notify(
                    post.getAuthorId(),
                    "FORUM",
                    "帖子有新回复",
                    author.getUsername() + "：" + preview,
                    "/forum/" + postId
            );
        }
        return saved;
    }

    @Override
    @Transactional
    public LikeToggleResult toggleLikePost(Long postId, Long userId) {
        ForumPost post = getPost(postId, false);
        boolean liked = likeService.toggle(userId, LikeTargetType.FORUM_POST, postId);
        int current = post.getLikeCount() != null ? post.getLikeCount() : 0;
        post.setLikeCount(liked ? current + 1 : Math.max(0, current - 1));
        postMapper.save(post);
        return new LikeToggleResult(liked, post.getLikeCount());
    }

    @Override
    public Map<String, Object> getPostDetail(Long id, Long viewerId) {
        ForumPost post = getPost(id, true);
        if (viewerId != null) {
            post.setLiked(likeService.isLiked(viewerId, LikeTargetType.FORUM_POST, id));
        }
        List<ForumReply> replies = listReplies(id);
        Map<String, Object> data = new HashMap<>();
        data.put("post", post);
        data.put("replies", replies);
        data.put("isAuthor", viewerId != null && viewerId.equals(post.getAuthorId()));
        data.put("isLiked", Boolean.TRUE.equals(post.getLiked()));
        return data;
    }

    @Override
    public List<ForumPost> listPostsForAdmin(String status, String keyword) {
        List<ForumPost> posts = postMapper.findAllByOrderByCreateTimeDesc();
        if (status != null && !status.isBlank() && !"all".equalsIgnoreCase(status)) {
            posts = posts.stream()
                    .filter(p -> status.equalsIgnoreCase(p.getStatus()))
                    .collect(Collectors.toList());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            posts = posts.stream()
                    .filter(p -> containsIgnoreCase(p.getTitle(), kw)
                            || containsIgnoreCase(p.getContent(), kw)
                            || containsIgnoreCase(p.getAuthorName(), kw))
                    .collect(Collectors.toList());
        }
        return posts;
    }

    @Override
    public Map<String, Object> getPostDetailForAdmin(Long id) {
        ForumPost post = postMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        List<ForumReply> replies = replyMapper.findByPostIdOrderByCreateTimeAsc(id);
        Map<String, Object> data = new HashMap<>();
        data.put("post", post);
        data.put("replies", replies);
        return data;
    }

    @Override
    @Transactional
    public void deleteReply(Long id, Long operatorId) {
        ForumReply reply = replyMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("回复不存在"));
        if (!AuthContext.isAdmin()) {
            throw new RuntimeException("无权删除该回复");
        }
        reply.setStatus("REMOVED");
        replyMapper.save(reply);
        Long postId = reply.getPostId();
        postMapper.findById(postId).ifPresent(post -> {
            post.setReplyCount((int) replyMapper.countByPostIdAndStatus(postId, "ACTIVE"));
            postMapper.save(post);
        });
    }

    private boolean containsIgnoreCase(String text, String kw) {
        return text != null && text.toLowerCase().contains(kw);
    }
}
