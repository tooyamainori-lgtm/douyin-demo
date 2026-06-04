package com.douyin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyin.entity.VideoFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 视频收藏 Mapper
 */
@Mapper
public interface VideoFavoriteMapper extends BaseMapper<VideoFavorite> {

    @Delete("DELETE FROM video_favorite WHERE user_id = #{userId} AND video_id = #{videoId}")
    int physicalDelete(@Param("userId") Long userId, @Param("videoId") Long videoId);
}
