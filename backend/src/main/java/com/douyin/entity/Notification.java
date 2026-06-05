package com.douyin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知表实体
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 接收通知的用户ID */
    private Long userId;

    /** 触发通知的用户ID */
    private Long fromUserId;

    /** 类型：like/comment/follow */
    private String type;

    /** 关联的视频ID（like/comment 时有值） */
    private Long videoId;

    /** 通知内容 */
    private String content;

    /** 是否已读 */
    private Boolean isRead;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
