package com.douyin.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 评论用户视图对象
 */
@Data
@Builder
public class CommentUserVO {

    /** 用户ID */
    private String id;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatarUrl;
}
