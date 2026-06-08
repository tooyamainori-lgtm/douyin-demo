package com.douyin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyin.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 聊天消息 Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /** 两人之间的最新一条消息 */
    @Select("SELECT * FROM chat_message WHERE (sender_id = #{userId1} AND receiver_id = #{userId2}) OR (sender_id = #{userId2} AND receiver_id = #{userId1}) ORDER BY create_time DESC LIMIT 1")
    ChatMessage findLatestMessage(Long userId1, Long userId2);

    /** 标记两人之间消息为已读 */
    @Update("UPDATE chat_message SET is_read = 1 WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND is_read = 0")
    int markRead(Long senderId, Long receiverId);

    /** 未读消息总数 */
    @Select("SELECT COUNT(*) FROM chat_message WHERE receiver_id = #{userId} AND is_read = 0")
    Long countUnread(Long userId);

    /** 某个联系人的未读数 */
    @Select("SELECT COUNT(*) FROM chat_message WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND is_read = 0")
    Long countUnreadFromUser(Long senderId, Long receiverId);
}
