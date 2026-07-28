package com.douyin.controller;

import com.douyin.common.Result;
import com.douyin.common.exception.BusinessException;
import com.douyin.entity.ChatMessage;
import com.douyin.entity.User;
import com.douyin.mapper.ChatMessageMapper;
import com.douyin.mapper.FollowMapper;
import com.douyin.mapper.UserMapper;
import com.douyin.vo.ChatMessageVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock private ChatMessageMapper chatMessageMapper;
    @Mock private FollowMapper followMapper;
    @Mock private UserMapper userMapper;
    @InjectMocks private ChatController controller;

    @Test
    void mutualFriendsCanSendMessage() {
        when(userMapper.selectById(2L)).thenReturn(new User());
        when(followMapper.selectCount(any())).thenReturn(1L, 1L);
        doAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setId(99L);
            return 1;
        }).when(chatMessageMapper).insert(any(ChatMessage.class));

        Result<ChatMessageVO> result = controller.send(2L, 1L, Map.of("content", "你好"));

        assertEquals(200, result.getCode());
        assertEquals("99", result.getData().getId());
        verify(chatMessageMapper).insert(any(ChatMessage.class));
    }

    @Test
    void nonMutualUsersCannotSendMessage() {
        when(userMapper.selectById(2L)).thenReturn(new User());
        when(followMapper.selectCount(any())).thenReturn(1L, 0L);

        BusinessException error = assertThrows(BusinessException.class,
                () -> controller.send(2L, 1L, Map.of("content", "你好")));

        assertEquals(403, error.getCode());
        verify(chatMessageMapper, never()).insert(any(ChatMessage.class));
    }
}
