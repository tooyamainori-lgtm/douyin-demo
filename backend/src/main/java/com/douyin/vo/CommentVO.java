package com.douyin.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 评论视图对象
 */
@Data
@Builder
public class CommentVO {

    /** 评论ID */
    private String id;

    /** 评论内容 */
    private String content;

    /** 点赞数 */
    private Long likeCount;

    /** 评论时间 */
    private String createTime;

    /** 父评论ID */
    private String parentId;

    /** 被回复的用户昵称 */
    private String replyNickname;

    /** 评论用户信息 */
    private CommentUserVO user;

    /** 子回复列表（一级评论包含回复） */
    private List<CommentVO> replies;
}
