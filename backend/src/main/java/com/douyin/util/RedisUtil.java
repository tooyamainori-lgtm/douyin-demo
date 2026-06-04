package com.douyin.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis 工具类 — 热门视频排行榜
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private static final String HOT_KEY = "video:hot";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 增加热度分数
     *
     * @param videoId 视频ID
     * @param score   增加的分值
     */
    public void addHotScore(Long videoId, double score) {
        redisTemplate.opsForZSet().incrementScore(HOT_KEY, videoId.toString(), score);
    }

    /**
     * 获取排行榜 Top N
     *
     * @param top 前 N 名
     * @return 视频ID → 热度分（有序）
     */
    public Map<Long, Double> getHotRank(int top) {
        Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_KEY, 0, top - 1);

        Map<Long, Double> result = new LinkedHashMap<>();
        if (set != null) {
            for (ZSetOperations.TypedTuple<Object> tuple : set) {
                Long videoId = Long.valueOf(tuple.getValue().toString());
                result.put(videoId, tuple.getScore());
            }
        }
        return result;
    }
}
