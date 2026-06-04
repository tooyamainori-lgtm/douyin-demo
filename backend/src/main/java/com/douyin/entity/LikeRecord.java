package com.douyin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞记录表实体
 */
@Data
@TableName("like_record")
public class LikeRecord {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 点赞用户ID */
    private Long userId;

    /** 被点赞视频ID */
    private Long videoId;

    /** 点赞时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 逻辑删除（取消点赞即为删除） */
    @TableLogic
    private Integer isDeleted;
}
