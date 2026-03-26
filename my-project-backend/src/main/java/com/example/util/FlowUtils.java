package com.example.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Component
public class FlowUtils {
     @Autowired
    StringRedisTemplate redisTemplate;
    private static final LimitAction defaultAction = (overclock) -> !overclock;//接收一个布尔类型的参数 overclock，并返回其逻辑取反的结果
    /**
     * 用户获取验证码限流，60s只获取一次
     */
     public boolean limitOneCheck(String key,int blockTime){
         //如果 Redis 中存在指定的键，则说明限流条件已达到，返回 false
         if(Boolean.TRUE.equals(redisTemplate.hasKey(key))){
             return false;
         }else {
             //将指定的键设置到 Redis 中，并指定过期时间为 blockTime 秒，然后返回 true
             redisTemplate.opsForValue()
                     .set(key,"",blockTime, TimeUnit.SECONDS);
             return true;
         }
     }
    /**
     * 针对于在时间段内多次请求限制，如3秒内20次请求
     * @param counterKey 计数键
     * @param frequency 请求频率
     * @param period 计数周期
     * @return 是否通过限流检查
     */
    public boolean limitPeriodCounterCheck(String counterKey, int frequency, int period){
        return this.internalCheck(counterKey, frequency, period, defaultAction);
    }

    /**
     * 内部使用请求限制主要逻辑
     * @param key 计数键
     * @param frequency 请求频率（请求次数）
     * @param period 计数周期,以秒为单位。
     * @param action 限制行为与策略
     * @return 是否通过限流检查
     */
    private boolean internalCheck(String key, int frequency, int period, LimitAction action){
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            //检查 Redis 中是否存在指定的键 key。如果存在，则对该键对应的值进行递增操作，并获取递增后的值。
            Long value = Optional.ofNullable(redisTemplate.opsForValue().increment(key)).orElse(0L);
            return action.run(value > frequency);//请求次数超出阀值，返回true
        } else {
            redisTemplate.opsForValue().set(key, "1", period, TimeUnit.SECONDS);
            return true;
        }
    }

    /**
     * 内部使用，限制行为与策略
     */
    private interface LimitAction {
        boolean run(boolean overclock);
    }
}
