package com.douyin.service;

import com.douyin.common.PageResult;
import com.douyin.vo.VideoVO;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    /**
     * 收藏视频
     */
    void add(Long videoId, Long userId);

    /**
     * 取消收藏
     */
    void remove(Long videoId, Long userId);

    /**
     * 我的收藏列表
     */
    PageResult<VideoVO> listMyFavorites(Long userId, Integer page, Integer size);

    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Long videoId, Long userId);
}
