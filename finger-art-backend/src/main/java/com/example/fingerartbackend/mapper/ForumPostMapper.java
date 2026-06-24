package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 论坛帖子实体 {@link ForumPost} 的数据访问层。
 * <p>
 * 负责社区帖子的持久化，支持按状态、热度及作者筛选。
 * </p>
 */
@Repository
public interface ForumPostMapper extends JpaRepository<ForumPost, Long> {

    /** 按状态查询帖子，按创建时间降序 */
    List<ForumPost> findByStatusOrderByCreateTimeDesc(String status);

    /** 按状态查询帖子，优先按回复数降序，其次按创建时间降序（热门排序） */
    List<ForumPost> findByStatusOrderByReplyCountDescCreateTimeDesc(String status);

    /** 查询全部帖子，按创建时间降序（管理员视图） */
    List<ForumPost> findAllByOrderByCreateTimeDesc();

    /** 按作者 ID 查询其发布的帖子，按创建时间降序 */
    List<ForumPost> findByAuthorIdOrderByCreateTimeDesc(Long authorId);
}
