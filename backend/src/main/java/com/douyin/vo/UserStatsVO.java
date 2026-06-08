package com.douyin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户创作者数据统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsVO {

    /** 总播放量 */
    private Long totalViews;

    /** 总获赞数 */
    private Long totalLikes;

    /** 总评论数 */
    private Long totalComments;

    /** 视频总数 */
    private Long videoCount;

    /** 粉丝数 */
    private Long fansCount;

    /** 近7天新增粉丝 */
    private Long recentFansGrowth;
}
