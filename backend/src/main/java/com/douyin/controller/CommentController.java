package com.douyin.controller;

import com.douyin.common.Result;
import com.douyin.dto.CommentDTO;
import com.douyin.service.CommentService;
import com.douyin.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 发表评论
     *
     * @param videoId 视频ID
     * @param dto     评论参数
     * @param userId  当前用户ID
     * @return 评论信息
     */
    @PostMapping("/videos/{videoId}/comments")
    public Result<CommentVO> publish(
            @PathVariable Long videoId,
            @Valid @RequestBody CommentDTO dto,
            @RequestAttribute("userId") Long userId) {
        dto.setVideoId(videoId);
        CommentVO vo = commentService.publish(dto, userId);
        return Result.ok(vo);
    }

    /**
     * 获取视频评论列表
     *
     * @param videoId 视频ID
     * @return 评论列表（一级评论 + 回复）
     */
    @GetMapping("/videos/{videoId}/comments")
    public Result<List<CommentVO>> listByVideo(@PathVariable Long videoId) {
        List<CommentVO> list = commentService.listByVideo(videoId);
        return Result.ok(list);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     * @return 成功
     */
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> delete(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long userId) {
        commentService.delete(commentId, userId);
        return Result.ok();
    }

    /**
     * 点赞评论
     */
    @PostMapping("/comments/{commentId}/like")
    public Result<Void> likeComment(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long userId) {
        commentService.likeComment(commentId, userId);
        return Result.ok();
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/comments/{commentId}/like")
    public Result<Void> unlikeComment(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long userId) {
        commentService.unlikeComment(commentId, userId);
        return Result.ok();
    }
}
