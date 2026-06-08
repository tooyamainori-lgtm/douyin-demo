package com.douyin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签视图对象 — 前端标签选择器 + 标签聚合页使用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagVO {

    /** 标签名称，如"搞笑""美食" */
    private String name;

    /** 关联视频数量 */
    private Long videoCount;

    /** 图标 emoji（前端展示用） */
    private String icon;
}
