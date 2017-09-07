package com.ycb.wxxcx.provider.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by zhuhui on 17-7-26.
 */
@Aspect
@Component
public class LoginAspect {
    public static final Logger logger = LoggerFactory.getLogger(LoginAspect.class);

    @Autowired
    private RedisTemplate<String, String> template;

    private ValueOperations<String, String> operations;

    //    @Pointcut("execution(public * com.didispace.web..*.*(..))")
    @Pointcut("@annotation(com.ycb.wxxcx.provider.aspect.Action)")
    public void log() {
    }

    @Before("log()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
        operations = template.opsForValue();
        String method = joinPoint.getSignature().getDeclaringTypeName() + '.' + joinPoint.getSignature().getName();
        logger.info("calling " + method);
        // 根据用户session 做权限检验
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            logger.info("arg: " + arg);
        }
        String session = (String) args[0];
        if (StringUtils.isEmpty(operations.get(session))){
            // 如果session失效  直接返回错误code
        }
    }

    @AfterReturning(returning = "ret", pointcut = "log()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
        logger.info("RESPONSE : " + ret);
    }
}
