package com.douyin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论表实体
 */
@Data
@TableName("comment")
public class Comment {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 评论用户ID */
    private Long userId;

    /** 所属视频ID */
    private Long videoId;

    /** 父评论ID（NULL 表示一级评论） */
    private Long parentId;

    /** 被回复的用户ID */
    private Long replyUserId;

    /** 评论内容 */
    private String content;

    /** 点赞数 */
    private Long likeCount;

    /** 评论时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;
}
