package com.douyin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.douyin.common.exception.BusinessException;
import com.douyin.dto.CommentDTO;
import com.douyin.entity.Comment;
import com.douyin.entity.User;
import com.douyin.mapper.CommentMapper;
import com.douyin.mapper.UserMapper;
import com.douyin.mapper.VideoMapper;
import com.douyin.service.CommentService;
import com.douyin.service.NotificationService;
import com.douyin.vo.CommentUserVO;
import com.douyin.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final VideoMapper videoMapper;
    private final NotificationService notificationService;

    @Override
    public CommentVO publish(CommentDTO dto, Long userId) {
        // 校验视频存在
        if (videoMapper.selectById(dto.getVideoId()) == null) {
            throw new BusinessException(2001, "视频不存在");
        }
        // 如果回复评论，校验父评论存在
        if (dto.getParentId() != null) {
            if (commentMapper.selectById(dto.getParentId()) == null) {
                throw new BusinessException(3001, "评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setVideoId(dto.getVideoId());
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId());
        comment.setReplyUserId(dto.getReplyUserId());
        comment.setLikeCount(0L);
        commentMapper.insert(comment);

        // 更新视频评论数
        com.douyin.entity.Video video = videoMapper.selectById(dto.getVideoId());
        video.setCommentCount(video.getCommentCount() + 1);
        videoMapper.updateById(video);

        // 通知视频作者（自己评论自己视频不通知）
        if (!video.getUserId().equals(userId)) {
            notificationService.create(video.getUserId(), userId, "comment", dto.getVideoId(), "评论了你的视频");
        }
        // 如果是回复评论，通知被回复者（不重复通知自己或视频作者）
        if (dto.getReplyUserId() != null && !dto.getReplyUserId().equals(userId) && !dto.getReplyUserId().equals(video.getUserId())) {
            notificationService.create(dto.getReplyUserId(), userId, "comment", dto.getVideoId(), "回复了你的评论");
        }

        return buildCommentVO(comment, userId);
    }

    @Override
    public List<CommentVO> listByVideo(Long videoId) {
        // 查询所有评论（按时间升序）
        List<Comment> allComments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getVideoId, videoId)
                        .orderByAsc(Comment::getCreateTime));

        // 分离一级评论和回复
        List<Comment> rootComments = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        Map<Long, List<Comment>> repliesMap = allComments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 构建评论树（二级回复：根 → 回复 → 对回复的回复）
        return rootComments.stream()
                .map(root -> buildReplyTree(root, repliesMap))
                .collect(Collectors.toList());
    }

    /**
     * 递归构建回复树
     */
    private CommentVO buildReplyTree(Comment comment, Map<Long, List<Comment>> repliesMap) {
        CommentVO vo = buildCommentVO(comment, comment.getUserId());
        List<Comment> children = repliesMap.getOrDefault(comment.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            List<CommentVO> childVOs = children.stream()
                    .map(child -> buildReplyTree(child, repliesMap))
                    .collect(Collectors.toList());
            vo.setReplies(childVOs);
        }
        return vo;
    }

    @Override
    public void delete(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(3001, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的评论");
        }
        commentMapper.deleteById(commentId);
    }

    /**
     * 构建 CommentVO
     */
    private CommentVO buildCommentVO(Comment comment, Long userId) {
        User user = userMapper.selectById(comment.getUserId());
        String replyNickname = null;
        if (comment.getReplyUserId() != null) {
            User replyUser = userMapper.selectById(comment.getReplyUserId());
            replyNickname = replyUser != null ? replyUser.getNickname() : null;
        }

        return CommentVO.builder()
                .id(comment.getId().toString())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .createTime(comment.getCreateTime() != null ? comment.getCreateTime().toString() : null)
                .parentId(comment.getParentId() != null ? comment.getParentId().toString() : null)
                .replyNickname(replyNickname)
                .user(CommentUserVO.builder()
                        .id(user.getId().toString())
                        .nickname(user.getNickname())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .replies(Collections.emptyList())
                .build();
    }
}
