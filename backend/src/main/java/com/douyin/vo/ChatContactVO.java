package com.douyin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天联系人 — 消息列表中的每个条目
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatContactVO {

    /** 联系人用户ID */
    private String userId;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatarUrl;

    /** 最新一条消息内容 */
    private String lastMessage;

    /** 最新消息时间 */
    private String lastTime;

    /** 未读数 */
    private Long unreadCount;
}
