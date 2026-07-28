package com.douyin.service;

import com.douyin.entity.Comment;
import com.douyin.mapper.CommentLikeMapper;
import com.douyin.mapper.CommentMapper;
import com.douyin.mapper.UserMapper;
import com.douyin.mapper.VideoMapper;
import com.douyin.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock private CommentMapper commentMapper;
    @Mock private CommentLikeMapper commentLikeMapper;
    @Mock private UserMapper userMapper;
    @Mock private VideoMapper videoMapper;
    @Mock private NotificationService notificationService;
    @InjectMocks private CommentServiceImpl service;

    @Test
    void deletingRootCommentDeletesRepliesAndUpdatesVideoCount() {
        Comment root = comment(1L, null);
        Comment child = comment(2L, 1L);
        Comment grandchild = comment(3L, 2L);
        when(commentMapper.selectById(1L)).thenReturn(root);
        when(commentMapper.selectList(any())).thenReturn(List.of(root, child, grandchild));

        service.delete(1L, 5L);

        verify(commentMapper).deleteById(1L);
        verify(commentMapper).deleteById(2L);
        verify(commentMapper).deleteById(3L);
        verify(videoMapper).decrementCommentCount(10L, 3);
    }

    private Comment comment(Long id, Long parentId) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setParentId(parentId);
        comment.setVideoId(10L);
        comment.setUserId(5L);
        return comment;
    }
}
