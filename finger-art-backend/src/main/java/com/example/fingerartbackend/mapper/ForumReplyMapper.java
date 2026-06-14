package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.ForumReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumReplyMapper extends JpaRepository<ForumReply, Long> {
    List<ForumReply> findByPostIdAndStatusOrderByCreateTimeAsc(Long postId, String status);

    List<ForumReply> findByPostIdOrderByCreateTimeAsc(Long postId);

    long countByPostIdAndStatus(Long postId, String status);
}
