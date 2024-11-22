package com.hsdb.limit.aspect;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@AutoConfigureAfter(RedissonClient.class)
public class RedissonRateLimitAspect {
    @Resource
    private RedissonClient redissonClient;

    @PostConstruct
    public void init() {
        log.info("Redisson的限流器被加载.........");
        if (redissonClient == null) {
            log.warn("Spring容器中没有RedissonClient，Redisson限流器将无法使用............");
        }
    }

    @Around("@annotation(redissonRateLimit)")
    public Object redissonRateLimitCheck(ProceedingJoinPoint joinPoint, RedissonRateLimit redissonRateLimit) throws Throwable {
        String key = getKey(joinPoint);
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        log.warn("RedissonRateLimit开始工作");
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        log.info("RedissonRateLimit key:{},timeOut:{},count:{},url:{}", key, redissonRateLimit.timeOut(), redissonRateLimit.count(), httpServletRequest.getRequestURI());

        RRateLimiter rateLimiter = getRedissonRateLimiter(redissonRateLimit, key);
        boolean result = rateLimiter.tryAcquire(1);
        if (!result) {
            throw new LimitException("当前访问人数过多，请稍后再试");
        }
        return joinPoint.proceed();
    }

    private String getKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取切入方法的对象
        Method method = signature.getMethod();
        //获取方法上的Aop注解
        RedissonRateLimit annotation = method.getAnnotation(RedissonRateLimit.class);
        //获取注解上的值如 : @RedissonRateLimit(key = "'param id is ' + #id")
        String keyEl = annotation.key();
        //将注解的值中的El表达式部分进行替换
        //创建解析器
        SpelExpressionParser parser = new SpelExpressionParser();
        //获取表达式
        Expression expression = parser.parseExpression(keyEl);
        //设置解析上下文(有哪些占位符，以及每种占位符的值)
        EvaluationContext context = new StandardEvaluationContext();
        //获取参数值
        Object[] args = joinPoint.getArgs();
        //获取运行时参数的名称
        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        String[] parameterNames = discoverer.getParameterNames(method);
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i].toString());
        }
        EvaluationContext cc = getContext(joinPoint.getArgs(), signature.getMethod());
        return getValue(cc, keyEl, String.class);
    }


    private RRateLimiter getRedissonRateLimiter(RedissonRateLimit redissonRateLimit, String key) {
        long count = redissonRateLimit.count();
        long timeOut = redissonRateLimit.timeOut();
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        if (!rateLimiter.isExists()) {
            rateLimiter.trySetRate(RateType.OVERALL, count, timeOut * 60, RateIntervalUnit.SECONDS);
            return rateLimiter;
        }
        return rateLimiter;
    }


    private <T> T getValue(EvaluationContext context, String key, Class<T> clazz) {
        SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
        Expression expression = spelExpressionParser.parseExpression(key);
        return expression.getValue(context, clazz);
    }

    private EvaluationContext getContext(Object[] arguments, Method signatureMethod) {
        String[] parameterNames = new StandardReflectionParameterNameDiscoverer().getParameterNames(signatureMethod);
        if (parameterNames == null) {
            throw new IllegalArgumentException("参数列表不能为null");
        }
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < arguments.length; i++) {
            context.setVariable(parameterNames[i], arguments[i]);
        }
        return context;
    }
}
