package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageMapper extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE (m.senderId = ?1 AND m.receiverId = ?2) OR (m.senderId = ?2 AND m.receiverId = ?1) ORDER BY m.createTime ASC")
    List<Message> findChatHistory(Long userId1, Long userId2);

    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);
    
    @Query("SELECT m FROM Message m WHERE m.receiverId = ?1 OR m.senderId = ?1 ORDER BY m.createTime DESC")
    List<Message> findAllUserMessages(Long userId);
}
