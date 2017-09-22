package com.ycb.zprovider.cache;

import org.apache.commons.lang.NullArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhuhui on 17-7-27.
 */
@Service
@PropertySource("classpath:bootstrap.properties")
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> template;

    public void setKeyValueTimeout(String key, String value, Integer timeout) {
        template.setValueSerializer(new GenericToStringSerializer<>(String.class));
        template.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    public String getKeyValue(String key) {
        if (StringUtils.isEmpty(key)) {
            throw new NullArgumentException("key is null.");
        }
        return template.opsForValue().get(key);
    }
}
