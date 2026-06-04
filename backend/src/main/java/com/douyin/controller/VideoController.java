package com.douyin.controller;

import com.douyin.common.PageResult;
import com.douyin.common.Result;
import com.douyin.service.VideoService;
import com.douyin.vo.VideoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频控制器
 */
@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /**
     * 上传视频
     *
     * @param file        视频文件
     * @param title       标题
     * @param description 描述
     * @param tags        标签
     * @param userId      当前用户ID（JWT）
     * @return 视频信息
     */
    @PostMapping
    public Result<VideoVO> upload(
            @RequestParam("video") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestAttribute("userId") Long userId) {
        VideoVO vo = videoService.upload(file, title, description, tags, userId);
        return Result.ok(vo);
    }

    /**
     * 视频分页列表（信息流 / 搜索）
     *
     * @param page    页码（默认 1）
     * @param size    每页条数（默认 10）
     * @param keyword 搜索关键词（可选）
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<VideoVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        PageResult<VideoVO> result = videoService.page(page, size, keyword);
        return Result.ok(result);
    }

    /**
     * 视频详情
     *
     * @param id 视频ID
     * @return 视频详情
     */
    @GetMapping("/{id}")
    public Result<VideoVO> getDetail(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        VideoVO vo = videoService.getDetail(id, userId);
        return Result.ok(vo);
    }
}
