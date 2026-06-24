package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 私信消息实体 {@link Message} 的数据访问层。
 * <p>
 * 负责用户间私信的持久化、会话历史及未读消息查询。
 * </p>
 */
@Repository
public interface MessageMapper extends JpaRepository<Message, Long> {

    /** 查询两用户之间的完整聊天记录，按发送时间升序 */
    @Query("SELECT m FROM Message m WHERE (m.senderId = ?1 AND m.receiverId = ?2) OR (m.senderId = ?2 AND m.receiverId = ?1) ORDER BY m.createTime ASC")
    List<Message> findChatHistory(Long userId1, Long userId2);

    /** 查询某用户收到的全部未读消息 */
    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);

    /** 查询某用户作为发送方或接收方的全部消息，按时间降序 */
    @Query("SELECT m FROM Message m WHERE m.receiverId = ?1 OR m.senderId = ?1 ORDER BY m.createTime DESC")
    List<Message> findAllUserMessages(Long userId);
}
