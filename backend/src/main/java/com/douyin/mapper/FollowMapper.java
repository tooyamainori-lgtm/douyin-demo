package com.douyin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyin.entity.Follow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 关注关系 Mapper
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    /** 物理删除关注记录 */
    @Delete("DELETE FROM follow WHERE follower_id = #{followerId} AND followee_id = #{followeeId}")
    int physicalDelete(Long followerId, Long followeeId);
}
