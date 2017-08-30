package com.ycb.wxxcx.provider.utils;

import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.constant.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Created by duxinyaun on 2017/8/28.
 */
public class AccessToken {

    public static final Logger logger = LoggerFactory.getLogger(AccessToken.class);

    @Autowired
    private RedisService redisService;

    @Value("${appID}")
    private String appID;

    @Value("${appSecret}")
    private String appSecret;

    public String getAccessToken() throws Exception{

        String ACCESS_TOKEN = "";
        // 检索redis，不存在的话重新向微信请求
        ACCESS_TOKEN = redisService.getKeyValue("ACCESS_TOKEN");

        if (StringUtils.isEmpty(ACCESS_TOKEN)) {
            String param = "grant_type=client_credential&appid="+appID+"&secret="+appSecret;
            try {
                String tokenInfo = HttpRequest.sendGet(GlobalConfig.WX_ACCESS_TOKEN_URL, param);
                Map<String, Object> tokenInfoMap = JsonUtils.readValue(tokenInfo);
                String accessToken = (String) tokenInfoMap.get("access_token");
                Integer expiresIn = (Integer) tokenInfoMap.get("expires_in");
                // 将accessToken存入Redis,存放时间为7200秒
                redisService.setKeyValueTimeout("ACCESS_TOKEN", accessToken, expiresIn);
                return accessToken;
            } catch (Exception e) {
                logger.error(e.getMessage());
                return null;
            }
        } else {
            return ACCESS_TOKEN;
        }
    }
}
