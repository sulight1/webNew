package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostMapper extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByStatusOrderByCreateTimeDesc(String status);

    List<ForumPost> findByStatusOrderByReplyCountDescCreateTimeDesc(String status);

    List<ForumPost> findAllByOrderByCreateTimeDesc();

    List<ForumPost> findByAuthorIdOrderByCreateTimeDesc(Long authorId);
}
