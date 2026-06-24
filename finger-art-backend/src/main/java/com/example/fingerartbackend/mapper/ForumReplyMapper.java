package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.ForumReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 论坛回复实体 {@link ForumReply} 的数据访问层。
 * <p>
 * 负责帖子下回复内容的持久化及按帖子/状态查询。
 * </p>
 */
@Repository
public interface ForumReplyMapper extends JpaRepository<ForumReply, Long> {

    /** 按帖子 ID 与审核状态查询回复，按创建时间升序 */
    List<ForumReply> findByPostIdAndStatusOrderByCreateTimeAsc(Long postId, String status);

    /** 按帖子 ID 查询全部回复，按创建时间升序 */
    List<ForumReply> findByPostIdOrderByCreateTimeAsc(Long postId);

    /** 统计某帖子在指定状态下的回复数量 */
    long countByPostIdAndStatus(Long postId, String status);
}
