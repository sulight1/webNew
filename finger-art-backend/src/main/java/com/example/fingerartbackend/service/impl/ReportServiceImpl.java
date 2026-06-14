package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.ContentReport;
import com.example.fingerartbackend.entity.ForumPost;
import com.example.fingerartbackend.entity.ForumReply;
import com.example.fingerartbackend.entity.Product;
import com.example.fingerartbackend.entity.Skill;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.ContentReportMapper;
import com.example.fingerartbackend.mapper.ForumPostMapper;
import com.example.fingerartbackend.mapper.ForumReplyMapper;
import com.example.fingerartbackend.mapper.ProductMapper;
import com.example.fingerartbackend.mapper.SkillMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.ReportService;
import com.example.fingerartbackend.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ContentReportMapper reportMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private SkillMapper skillMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ForumPostMapper forumPostMapper;
    @Autowired
    private ForumReplyMapper forumReplyMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Override
    @Transactional
    public ContentReport submitReport(ContentReport report) {
        if (report.getReporterId() == null || report.getTargetType() == null || report.getTargetId() == null) {
            throw new RuntimeException("举报信息不完整");
        }
        if (report.getReason() == null || report.getReason().isBlank()) {
            throw new RuntimeException("请填写举报原因");
        }
        sensitiveWordService.validateText(report.getDetail(), "举报说明");
        report.setStatus("PENDING");
        if (report.getTargetTitle() == null) {
            report.setTargetTitle(resolveTargetTitle(report.getTargetType(), report.getTargetId()));
        }
        return reportMapper.save(report);
    }

    @Override
    public List<ContentReport> listReports(String status) {
        if (status != null && !status.isBlank()) {
            return reportMapper.findByStatusOrderByCreatedAtDesc(status);
        }
        return reportMapper.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public ContentReport handleReport(Long id, Long handlerId, String action, String handleNote) {
        ContentReport report = reportMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("举报记录不存在"));
        if (!"PENDING".equals(report.getStatus())) {
            throw new RuntimeException("该举报已处理");
        }

        report.setHandlerId(handlerId);
        report.setHandleNote(handleNote);
        report.setHandledAt(LocalDateTime.now());

        if ("REJECT".equalsIgnoreCase(action)) {
            report.setStatus("REJECTED");
        } else {
            report.setStatus("HANDLED");
            applyModerationAction(report, action);
        }

        ContentReport saved = reportMapper.save(report);
        if (report.getReporterId() != null) {
            notificationService.notify(
                    report.getReporterId(),
                    "REPORT",
                    "举报处理结果",
                    "你举报的「" + report.getTargetTitle() + "」已处理",
                    "/admin");
        }
        return saved;
    }

    @Override
    public long countPending() {
        return reportMapper.countByStatus("PENDING");
    }

    private void applyModerationAction(ContentReport report, String action) {
        String type = report.getTargetType();
        Long targetId = report.getTargetId();
        if ("PRODUCT".equals(type) && ("HIDE".equalsIgnoreCase(action) || "HANDLE".equalsIgnoreCase(action))) {
            productMapper.findById(targetId).ifPresent(p -> {
                p.setStatus("REJECTED");
                productMapper.save(p);
            });
        } else if ("SKILL".equals(type) && ("HIDE".equalsIgnoreCase(action) || "HANDLE".equalsIgnoreCase(action))) {
            skillMapper.findById(targetId).ifPresent(s -> {
                s.setStatus("REJECTED");
                skillMapper.save(s);
            });
        } else if ("USER".equals(type) && "WARN".equalsIgnoreCase(action)) {
            userMapper.findById(targetId).ifPresent(u -> {
                int score = u.getCreditScore() != null ? u.getCreditScore() : 100;
                u.setCreditScore(Math.max(0, score - 10));
                userMapper.save(u);
            });
        } else if ("FORUM_POST".equals(type) && ("HIDE".equalsIgnoreCase(action) || "HANDLE".equalsIgnoreCase(action))) {
            forumPostMapper.findById(targetId).ifPresent(post -> {
                post.setStatus("REMOVED");
                forumPostMapper.save(post);
            });
        } else if ("FORUM_REPLY".equals(type) && ("HIDE".equalsIgnoreCase(action) || "HANDLE".equalsIgnoreCase(action))) {
            forumReplyMapper.findById(targetId).ifPresent(reply -> {
                reply.setStatus("REMOVED");
                forumReplyMapper.save(reply);
                Long postId = reply.getPostId();
                forumPostMapper.findById(postId).ifPresent(post -> {
                    post.setReplyCount((int) forumReplyMapper.countByPostIdAndStatus(postId, "ACTIVE"));
                    forumPostMapper.save(post);
                });
            });
        }
    }

    private String resolveTargetTitle(String type, Long id) {
        if ("PRODUCT".equals(type)) {
            return productMapper.findById(id).map(Product::getTitle).orElse("作品#" + id);
        }
        if ("SKILL".equals(type)) {
            return skillMapper.findById(id).map(Skill::getTitle).orElse("技能#" + id);
        }
        if ("USER".equals(type)) {
            return userMapper.findById(id).map(User::getUsername).orElse("用户#" + id);
        }
        if ("FORUM_POST".equals(type)) {
            return forumPostMapper.findById(id).map(ForumPost::getTitle).orElse("帖子#" + id);
        }
        if ("FORUM_REPLY".equals(type)) {
            return forumReplyMapper.findById(id).map(reply -> {
                String preview = reply.getContent();
                if (preview != null && preview.length() > 30) {
                    preview = preview.substring(0, 30) + "...";
                }
                return "回复：" + (preview != null ? preview : "#" + id);
            }).orElse("回复#" + id);
        }
        return type + "#" + id;
    }
}
