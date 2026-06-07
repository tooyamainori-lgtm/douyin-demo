package com.douyin.service;

import com.douyin.common.PageResult;
import com.douyin.vo.VideoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 视频服务接口
 */
public interface VideoService {

    /**
     * 上传视频
     *
     * @param file        视频文件
     * @param title       标题
     * @param description 描述
     * @param tags        标签
     * @param userId      上传者ID
     * @return 视频信息
     */
    VideoVO upload(MultipartFile file, String title, String description, String tags, MultipartFile cover, Long userId);

    /**
     * 分页查询视频列表
     *
     * @param page   页码
     * @param size   每页条数
     * @param keyword 搜索关键词（可选）
     * @return 分页结果
     */
    PageResult<VideoVO> page(Integer page, Integer size, String keyword);

    /**
     * 获取视频详情
     *
     * @param videoId 视频ID
     * @param userId  当前用户ID（可为null）
     * @return 视频详情
     */
    VideoVO getDetail(Long videoId, Long userId);

    /**
     * 播放量 +1
     *
     * @param videoId 视频ID
     */
    void recordView(Long videoId);

    /**
     * 点赞视频
     *
     * @param videoId 视频ID
     * @param userId  用户ID
     */
    void like(Long videoId, Long userId);

    /**
     * 取消点赞
     *
     * @param videoId 视频ID
     * @param userId  用户ID
     */
    void unlike(Long videoId, Long userId);

    /**
     * 获取用户发布的视频列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页条数
     * @return 分页结果
     */
    PageResult<VideoVO> getUserVideos(Long userId, Integer page, Integer size);

    /**
     * 删除视频（仅作者可删）
     *
     * @param videoId 视频ID
     * @param userId  操作者ID
     */
    void delete(Long videoId, Long userId);

    /**
     * 我点赞的视频列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页条数
     * @return 分页结果
     */
    PageResult<VideoVO> getMyLikes(Long userId, Integer page, Integer size);

    /** 批量为无封面视频生成封面 */
    int batchGenerateCovers();

    /**
     * 获取热门视频排行榜
     *
     * @param top 前 N 名
     * @return 视频列表
     */
    List<VideoVO> getHotRank(int top);

    /**
     * 获取关注用户的视频动态（关注 Feed）
     *
     * @param userId 当前用户ID
     * @param page   页码
     * @param size   每页条数
     * @return 分页结果
     */
    PageResult<VideoVO> getFollowingFeed(Long userId, Integer page, Integer size);
}
