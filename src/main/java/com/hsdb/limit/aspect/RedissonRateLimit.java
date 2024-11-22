package com.hsdb.limit.aspect;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RedissonRateLimit {
    /**
     * 限流标识key，每个http接口都应该有一个唯一的key。
     */
    String key();

    /**
     * 限流的时间(单位为:分钟)，比如1分钟内最多1000个请求。注意我们这个限流器不是很精确，但误差不会太大
     */
    long timeOut();

    /**
     * 限流的次数，比如1分钟内最多1000个请求。注意count的值不能小于1,必须大于等于1
     */
    long count();

}
