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
        Map<Object, Double> raw = zReverseRangeWithScores(HOT_KEY, 0, top - 1);
        Map<Long, Double> result = new LinkedHashMap<>();
        raw.forEach((k, v) -> result.put(Long.valueOf(k.toString()), v));
        return result;
    }

    /**
     * 通用 ZSet 逆序范围查询（带分数）
     *
     * @param key   Redis key
     * @param start 起始位置
     * @param end   结束位置
     * @return member → score（有序）
     */
    public Map<Object, Double> zReverseRangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, start, end);

        Map<Object, Double> result = new LinkedHashMap<>();
        if (set != null) {
            for (ZSetOperations.TypedTuple<Object> tuple : set) {
                result.put(tuple.getValue(), tuple.getScore());
            }
        }
        return result;
    }

    /**
     * ZSet 增加分数
     *
     * @param key    Redis key
     * @param member 成员
     * @param score  分数
     */
    public void zIncrBy(String key, String member, double score) {
        redisTemplate.opsForZSet().incrementScore(key, member, score);
    }
}
