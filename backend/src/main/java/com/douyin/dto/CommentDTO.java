package com.douyin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求参数
 */
@Data
public class CommentDTO {

    /** 视频ID（由 Controller 从 URL 注入） */
    private Long videoId;

    /** 评论内容 */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;

    /** 父评论ID（回复评论时传入，一级评论不传） */
    private Long parentId;

    /** 被回复的用户ID（回复评论时传入） */
    private Long replyUserId;
}
