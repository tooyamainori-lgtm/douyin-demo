package com.douyin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyin.entity.WatchHistory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 观看历史 Mapper
 */
@Mapper
public interface WatchHistoryMapper extends BaseMapper<WatchHistory> {

    /** 清空用户观看历史 */
    @Delete("DELETE FROM watch_history WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    /** 删除单条观看记录 */
    @Delete("DELETE FROM watch_history WHERE user_id = #{userId} AND video_id = #{videoId}")
    int deleteByUserAndVideo(Long userId, Long videoId);
}
