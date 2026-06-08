package com.douyin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {

    /** 消息ID */
    private String id;

    /** 发送者ID */
    private String senderId;

    /** 接收者ID */
    private String receiverId;

    /** 消息内容 */
    private String content;

    /** 是否由自己发出 */
    @JsonProperty("isMine")
    private boolean isMine;

    /** 是否已读 */
    @JsonProperty("isRead")
    private Boolean isRead;

    /** 发送时间 HH:mm */
    private String time;
}
