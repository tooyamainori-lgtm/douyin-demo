package com.douyin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyin.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 评论 Mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{commentId} AND is_deleted = 0")
    int incrementLikeCount(Long commentId);

    @Update("UPDATE comment SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{commentId} AND is_deleted = 0")
    int decrementLikeCount(Long commentId);
}
