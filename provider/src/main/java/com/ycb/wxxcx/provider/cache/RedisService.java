package com.ycb.wxxcx.provider.cache;

import org.apache.commons.lang.NullArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhuhui on 17-7-27.
 */
//@Component
@Service
@PropertySource("classpath:bootstrap.properties")
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> template;

    public void setKeyValueTimeout(String key, String value, Integer timeout) {
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
        template.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    public String getKeyValue(String key) {
        if (StringUtils.isEmpty(key)) {
            throw new NullArgumentException("key is null.");
        }
        return template.opsForValue().get(key);
    }
}
