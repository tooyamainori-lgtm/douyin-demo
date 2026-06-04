package com.douyin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关注关系表实体
 */
@Data
@TableName("follow")
public class Follow {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关注者用户ID */
    private Long followerId;

    /** 被关注者用户ID */
    private Long followeeId;

    /** 关注时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;
}
