package com.douyin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyin.entity.LikeRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 点赞记录 Mapper
 */
@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {

    /**
     * 物理删除点赞记录（取消点赞）
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 删除行数
     */
    @Delete("DELETE FROM like_record WHERE user_id = #{userId} AND video_id = #{videoId}")
    int physicalDelete(@Param("userId") Long userId, @Param("videoId") Long videoId);
}
