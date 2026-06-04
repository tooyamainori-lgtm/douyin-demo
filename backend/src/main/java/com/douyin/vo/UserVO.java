package com.douyin.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户视图对象
 */
@Data
@Builder
public class UserVO {

    /** 用户ID */
    private String id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatarUrl;

    /** JWT Token（仅登录/注册时返回） */
    private String token;
}
