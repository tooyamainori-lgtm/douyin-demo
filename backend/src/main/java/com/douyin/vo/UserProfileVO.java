package com.douyin.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户主页视图对象
 */
@Data
@Builder
public class UserProfileVO {

    /** 用户ID */
    private String id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatarUrl;

    /** 个人简介 */
    private String bio;

    /** 关注数 */
    private Long followCount;

    /** 粉丝数 */
    private Long fansCount;

    /** 视频数 */
    private Long videoCount;

    /** 获赞数 */
    private Long likeCount;

    /** 是否已关注（当前登录用户视角） */
    private Boolean isFollowing;

    /** 注册时间 */
    private String createTime;
}
