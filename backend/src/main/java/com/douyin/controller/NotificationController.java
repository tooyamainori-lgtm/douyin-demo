package com.douyin.controller;

import com.douyin.common.PageResult;
import com.douyin.common.Result;
import com.douyin.service.NotificationService;
import com.douyin.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** 通知列表 */
    @GetMapping("/notifications")
    public Result<PageResult<NotificationVO>> list(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.ok(notificationService.list(userId, page, size));
    }

    /** 未读数量 */
    @GetMapping("/notifications/unread-count")
    public Result<Long> unreadCount(@RequestAttribute("userId") Long userId) {
        return Result.ok(notificationService.countUnread(userId));
    }

    /** 标记全部已读 */
    @PutMapping("/notifications/read")
    public Result<Void> markAllRead(@RequestAttribute("userId") Long userId) {
        notificationService.markAllRead(userId);
        return Result.ok();
    }
}
