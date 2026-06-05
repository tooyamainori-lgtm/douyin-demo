package com.douyin.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 通知中的用户简要信息
 */
@Data
@Builder
public class NotificationUserVO {
    private String id;
    private String nickname;
    private String avatarUrl;
}
