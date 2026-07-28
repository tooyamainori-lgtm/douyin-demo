package com.douyin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.douyin.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 视频 Mapper
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    @Update("UPDATE video SET view_count = view_count + 1 WHERE id = #{videoId} AND is_deleted = 0")
    int incrementViewCount(Long videoId);

    @Update("UPDATE video SET like_count = like_count + 1 WHERE id = #{videoId} AND is_deleted = 0")
    int incrementLikeCount(Long videoId);

    @Update("UPDATE video SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{videoId} AND is_deleted = 0")
    int decrementLikeCount(Long videoId);

    @Update("UPDATE video SET comment_count = comment_count + 1 WHERE id = #{videoId} AND is_deleted = 0")
    int incrementCommentCount(Long videoId);

    @Update("UPDATE video SET comment_count = GREATEST(comment_count - #{count}, 0) WHERE id = #{videoId} AND is_deleted = 0")
    int decrementCommentCount(Long videoId, int count);

    /** 查询用户所有视频的总获赞数 */
    @Select("SELECT COALESCE(SUM(like_count), 0) FROM video WHERE user_id = #{userId} AND is_deleted = 0")
    Long getTotalLikesByUserId(Long userId);

    /** 查询所有无封面且是 MinIO 视频的 */
    @Select("SELECT * FROM video WHERE status = 1 AND is_deleted = 0 AND (cover_url = '' OR cover_url IS NULL) AND video_url LIKE 'http://%'")
    List<Video> selectWithoutCover();

    /** 用户所有视频的总播放量 */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM video WHERE user_id = #{userId} AND status = 1 AND is_deleted = 0")
    Long getTotalViewsByUserId(Long userId);

    /** 用户所有视频的总评论数 */
    @Select("SELECT COALESCE(SUM(comment_count), 0) FROM video WHERE user_id = #{userId} AND status = 1 AND is_deleted = 0")
    Long getTotalCommentsByUserId(Long userId);
}
