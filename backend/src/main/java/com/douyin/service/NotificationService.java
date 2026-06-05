package com.douyin.service;

import com.douyin.common.PageResult;
import com.douyin.vo.NotificationVO;

/**
 * 消息通知服务
 */
public interface NotificationService {

    /** 创建通知 */
    void create(Long userId, Long fromUserId, String type, Long videoId, String content);

    /** 分页获取通知列表 */
    PageResult<NotificationVO> list(Long userId, Integer page, Integer size);

    /** 标记全部已读 */
    void markAllRead(Long userId);

    /** 未读数量 */
    long countUnread(Long userId);
}
