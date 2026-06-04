package com.douyin.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应体
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 总条数 */
    private Long total;

    /** 当前页码 */
    private Long page;

    /** 每页条数 */
    private Long size;

    /** 数据列表 */
    private List<T> records;

    /**
     * 构建分页结果
     *
     * @param total   总条数
     * @param page    当前页码
     * @param size    每页条数
     * @param records 数据列表
     * @param <T>     数据类型
     * @return PageResult
     */
    public static <T> PageResult<T> of(Long total, Long page, Long size, List<T> records) {
        return new PageResult<>(total, page, size, records);
    }
}
