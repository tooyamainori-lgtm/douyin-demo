package com.douyin.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 通知视图对象
 */
@Data
@Builder
public class NotificationVO {
    private String id;
    private String type;
    private String content;
    private Boolean isRead;
    private String videoId;
    private String createTime;
    private NotificationUserVO fromUser;
}
