package com.douyin.controller;

import com.douyin.common.PageResult;
import com.douyin.common.Result;
import com.douyin.service.VideoService;
import com.douyin.vo.TagVO;
import com.douyin.vo.VideoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器 — 预设标签浏览与发现
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TagController {

    private final VideoService videoService;

    /** 预设标签列表（含视频数量） */
    @GetMapping("/tags")
    public Result<List<TagVO>> listTags() {
        return Result.ok(videoService.listTags());
    }

    /** 按标签浏览视频 */
    @GetMapping("/tags/{tagName}/videos")
    public Result<PageResult<VideoVO>> videosByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(videoService.getByTag(tagName, page, size));
    }
}
