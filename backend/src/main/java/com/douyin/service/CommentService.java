package com.douyin.service;

import com.douyin.dto.CommentDTO;
import com.douyin.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 发表评论
     *
     * @param dto    评论参数
     * @param userId 当前用户ID
     * @return 评论信息
     */
    CommentVO publish(CommentDTO dto, Long userId);

    /**
     * 获取视频的评论列表（一级评论 + 回复）
     *
     * @param videoId 视频ID
     * @return 评论树列表
     */
    List<CommentVO> listByVideo(Long videoId);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     */
    void delete(Long commentId, Long userId);
}
