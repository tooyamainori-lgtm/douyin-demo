package com.douyin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyin.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 视频 Mapper
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    /** 查询用户所有视频的总获赞数 */
    @Select("SELECT COALESCE(SUM(like_count), 0) FROM video WHERE user_id = #{userId} AND is_deleted = 0")
    Long getTotalLikesByUserId(Long userId);
}
