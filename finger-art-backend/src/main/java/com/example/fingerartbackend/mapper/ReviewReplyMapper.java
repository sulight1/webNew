package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评价回复实体 {@link ReviewReply} 的数据访问层。
 * <p>
 * 负责评价下回复内容的持久化及按评价 ID 的查询与清理。
 * </p>
 */
@Repository
public interface ReviewReplyMapper extends JpaRepository<ReviewReply, Long> {

    /** 按评价 ID 查询回复列表，按创建时间升序 */
    List<ReviewReply> findByReviewIdOrderByCreatedAtAsc(Long reviewId);

    /** 删除某评价下的全部回复（评价删除时级联清理） */
    void deleteByReviewId(Long reviewId);
}
