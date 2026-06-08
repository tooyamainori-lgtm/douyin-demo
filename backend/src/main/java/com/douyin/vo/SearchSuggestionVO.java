package com.douyin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索联想建议
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionVO {

    /** 建议词 */
    private String keyword;

    /** 类型：title-标题匹配 tag-标签匹配 hot-热门搜索 */
    private String type;

    /** 关联视频数 */
    private Long count;
}
