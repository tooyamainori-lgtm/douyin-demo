package com.douyin.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 视频作者视图对象
 */
@Data
@Builder
public class VideoAuthorVO {

    /** 作者ID */
    private String id;

    /** 作者昵称 */
    private String nickname;

    /** 作者头像 */
    private String avatarUrl;
}
