package com.douyin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.douyin.common.Result;
import com.douyin.entity.Video;
import com.douyin.mapper.VideoMapper;
import com.douyin.util.RedisUtil;
import com.douyin.vo.SearchSuggestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索控制器 — 联想建议 + 热搜
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final VideoMapper videoMapper;
    private final RedisUtil redisUtil;

    /** Redis key：热门搜索词排行榜 */
    private static final String HOT_SEARCH_KEY = "hot_search";

    /**
     * 搜索联想
     *
     * @param keyword 输入的关键词
     * @param limit   返回条数（默认 8）
     * @return 联想词列表
     */
    @GetMapping("/suggestions")
    public Result<List<SearchSuggestionVO>> suggest(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "8") int limit) {

        limit = Math.max(1, Math.min(limit, 20));

        List<SearchSuggestionVO> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // 1. 标题前缀匹配（优先）
        if (StringUtils.hasText(keyword)) {
            List<Video> titleMatches = videoMapper.selectList(
                    new LambdaQueryWrapper<Video>()
                            .select(Video::getTitle, Video::getViewCount)
                            .eq(Video::getStatus, 1)
                            .likeRight(Video::getTitle, keyword)
                            .orderByDesc(Video::getViewCount)
                            .last("LIMIT " + limit));
            for (Video v : titleMatches) {
                if (seen.add(v.getTitle())) {
                    result.add(SearchSuggestionVO.builder()
                            .keyword(v.getTitle())
                            .type("title")
                            .count(v.getViewCount())
                            .build());
                }
            }
        }

        // 2. 标签匹配
        if (StringUtils.hasText(keyword) && result.size() < limit) {
            List<Video> tagMatches = videoMapper.selectList(
                    new LambdaQueryWrapper<Video>()
                            .select(Video::getTags)
                            .eq(Video::getStatus, 1)
                            .like(Video::getTags, keyword)
                            .last("LIMIT " + limit));
            for (Video v : tagMatches) {
                if (StringUtils.hasText(v.getTags())) {
                    Arrays.stream(v.getTags().split(","))
                            .map(String::trim)
                            .filter(t -> t.contains(keyword) && seen.add("tag:" + t))
                            .forEach(t -> result.add(SearchSuggestionVO.builder()
                                    .keyword(t)
                                    .type("tag")
                                    .count(null)
                                    .build()));
                }
            }
        }

        // 3. 热门搜索补充（输入为空或结果不够时）
        if (result.size() < limit) {
            Map<Object, Double> hotMap = redisUtil.zReverseRangeWithScores(HOT_SEARCH_KEY, 0, limit - 1);
            hotMap.entrySet().stream()
                    .filter(e -> seen.add("hot:" + e.getKey().toString()))
                    .map(e -> SearchSuggestionVO.builder()
                            .keyword(e.getKey().toString())
                            .type("hot")
                            .count(e.getValue().longValue())
                            .build())
                    .limit(limit - result.size())
                    .forEach(result::add);
        }

        return Result.ok(result.stream().limit(limit).collect(Collectors.toList()));
    }
}
