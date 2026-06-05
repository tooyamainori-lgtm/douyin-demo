package com.douyin.service;

import com.douyin.vo.UserVO;

import java.util.List;

/**
 * 关注服务接口
 */
public interface FollowService {

    /**
     * 关注用户
     *
     * @param userId   当前用户ID
     * @param targetId 要关注的用户ID
     */
    void follow(Long userId, Long targetId);

    /**
     * 取消关注
     *
     * @param userId   当前用户ID
     * @param targetId 要取关的用户ID
     */
    void unfollow(Long userId, Long targetId);

    /**
     * 判断是否已关注
     *
     * @param userId   当前用户ID
     * @param targetId 目标用户ID
     * @return true=已关注
     */
    boolean isFollowing(Long userId, Long targetId);

    /**
     * 获取关注列表（我关注的人）
     *
     * @param userId 用户ID
     * @return 关注列表
     */
    List<UserVO> getFollowing(Long userId);

    /**
     * 获取粉丝列表（关注我的人）
     *
     * @param userId 用户ID
     * @return 粉丝列表
     */
    List<UserVO> getFollowers(Long userId);

    /**
     * 关注数
     */
    long countFollowing(Long userId);

    /**
     * 粉丝数
     */
    long countFollowers(Long userId);
}
