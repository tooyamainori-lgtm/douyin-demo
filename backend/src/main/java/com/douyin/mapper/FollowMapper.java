package com.douyin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyin.entity.Follow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 关注关系 Mapper
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    /** 物理删除关注记录 */
    @Delete("DELETE FROM follow WHERE follower_id = #{followerId} AND followee_id = #{followeeId}")
    int physicalDelete(Long followerId, Long followeeId);

    /** 近 N 天新增粉丝数 */
    @Select("SELECT COUNT(*) FROM follow WHERE followee_id = #{userId} AND create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    Long countRecentFollowers(Long userId, int days);
}
