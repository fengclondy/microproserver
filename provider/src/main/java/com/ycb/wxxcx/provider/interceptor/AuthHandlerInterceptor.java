package com.ycb.wxxcx.provider.interceptor;

import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhuhui on 17-7-27.
 */
public class AuthHandlerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String session = request.getParameter("session");
        if (StringUtils.isEmpty(session) || StringUtils.isEmpty(redisService.getKeyValue(session))) {
            // 如果session失效  直接返回错误code
            Map<String, Object> bacMap = new HashMap<>();
            bacMap.put("data", null);
            bacMap.put("code", 5);
            bacMap.put("msg", "用户session过期");
            String result = JsonUtils.writeValueAsString(bacMap);
            response.setContentType("application/json;charset=utf-8");
            response.getOutputStream().write(result.getBytes());
            return false;
        }
        return true;
    }
}
