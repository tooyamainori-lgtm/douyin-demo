package com.douyin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.douyin.common.Result;
import com.douyin.entity.ChatMessage;
import com.douyin.entity.Follow;
import com.douyin.entity.User;
import com.douyin.mapper.ChatMessageMapper;
import com.douyin.mapper.FollowMapper;
import com.douyin.mapper.UserMapper;
import com.douyin.vo.ChatContactVO;
import com.douyin.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天控制器 — 好友（互关）私信
 */
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageMapper chatMessageMapper;
    private final FollowMapper followMapper;
    private final UserMapper userMapper;
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 联系人列表 — 互关好友 + 最近聊天记录
     */
    @GetMapping("/contacts")
    public Result<List<ChatContactVO>> contacts(@RequestAttribute("userId") Long userId) {
        // 互关好友 = 我关注的人 且 对方也关注了我
        List<Long> myFollowees = followMapper.selectList(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, userId)
                        .select(Follow::getFolloweeId))
                .stream().map(Follow::getFolloweeId).toList();

        Set<Long> friendIds = new LinkedHashSet<>();
        for (Long followeeId : myFollowees) {
            boolean isMutual = followMapper.selectCount(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowerId, followeeId)
                            .eq(Follow::getFolloweeId, userId)) > 0;
            if (isMutual) friendIds.add(followeeId);
        }

        // 也包含有过聊天记录但非互关的用户
        List<ChatMessage> allMsgs = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .and(w -> w.eq(ChatMessage::getSenderId, userId).or().eq(ChatMessage::getReceiverId, userId))
                        .orderByDesc(ChatMessage::getCreateTime));
        for (ChatMessage m : allMsgs) {
            Long otherId = m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId();
            friendIds.add(otherId);
        }

        // 组装联系人列表
        List<ChatContactVO> contacts = new ArrayList<>();
        for (Long friendId : friendIds) {
            User friend = userMapper.selectById(friendId);
            if (friend == null) continue;
            ChatMessage lastMsg = chatMessageMapper.findLatestMessage(userId, friendId);
            Long unread = chatMessageMapper.countUnreadFromUser(friendId, userId);
            contacts.add(ChatContactVO.builder()
                    .userId(friend.getId().toString())
                    .nickname(friend.getNickname())
                    .avatarUrl(friend.getAvatarUrl())
                    .lastMessage(lastMsg != null ? lastMsg.getContent() : "你们是好友了，开始聊天吧")
                    .lastTime(lastMsg != null ? lastMsg.getCreateTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) : null)
                    .unreadCount(unread)
                    .build());
        }
        // 按最新消息时间排序
        contacts.sort((a, b) -> {
            if (a.getLastTime() == null) return 1;
            if (b.getLastTime() == null) return -1;
            return b.getLastTime().compareTo(a.getLastTime());
        });
        return Result.ok(contacts);
    }

    /**
     * 与某人的聊天记录
     */
    @GetMapping("/{userId}")
    public Result<List<ChatMessageVO>> history(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId,
            @RequestParam(defaultValue = "50") int limit) {
        List<ChatMessage> msgs = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .and(w -> w
                                .and(w1 -> w1.eq(ChatMessage::getSenderId, currentUserId)
                                        .eq(ChatMessage::getReceiverId, userId))
                                .or(w1 -> w1.eq(ChatMessage::getSenderId, userId)
                                        .eq(ChatMessage::getReceiverId, currentUserId)))
                        .orderByAsc(ChatMessage::getCreateTime)
                        .last("LIMIT " + limit));

        // 标记对方发来的未读消息为已读
        chatMessageMapper.markRead(userId, currentUserId);

        List<ChatMessageVO> vos = msgs.stream().map(m -> ChatMessageVO.builder()
                .id(m.getId().toString())
                .senderId(m.getSenderId().toString())
                .receiverId(m.getReceiverId().toString())
                .content(m.getContent())
                .isMine(m.getSenderId().equals(currentUserId))
                .isRead(m.getIsRead() == 1)
                .time(m.getCreateTime().format(TF))
                .build()).collect(Collectors.toList());
        return Result.ok(vos);
    }

    /**
     * 发送消息
     */
    @PostMapping("/{userId}")
    public Result<ChatMessageVO> send(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId,
            @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail(400, "消息内容不能为空");
        }
        ChatMessage msg = new ChatMessage();
        msg.setSenderId(currentUserId);
        msg.setReceiverId(userId);
        msg.setContent(content.trim());
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(msg);

        return Result.ok(ChatMessageVO.builder()
                .id(msg.getId().toString())
                .senderId(msg.getSenderId().toString())
                .receiverId(msg.getReceiverId().toString())
                .content(msg.getContent())
                .isMine(true)
                .isRead(false)
                .time(msg.getCreateTime().format(TF))
                .build());
    }

    /**
     * 未读消息总数
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestAttribute("userId") Long userId) {
        return Result.ok(chatMessageMapper.countUnread(userId));
    }
}
