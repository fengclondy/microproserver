package com.ycb.wxxcx.provider.utils;

import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yunchongba on 2017/8/23.
 */
public class TimeUtil {

    public static String calLastTime(String returnTime, String borrowTime, Long duration) {
        Long diff = 0L;
        String lastTime = null;
        if (StringUtils.isEmpty(borrowTime)){
            return "";
        }
        if (StringUtils.isEmpty(returnTime)) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 格式化时间
            String nowtime = df.format(new Date());// 将当前时间转换成字符串
            try {
                Date d1 = df.parse(nowtime);
                Date d2 = df.parse(borrowTime);
                diff = (d1.getTime() - d2.getTime()) / 1000;//这样得到的差值是秒
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else {
            diff = duration;
        }
        try {
            if (null != diff || 0 != diff.longValue()){
                Long days = diff / (60 * 60 * 24);   //天
                Long hours = (diff - days * (60 * 60 * 24)) / (60 * 60);  //小时
                Long minutes = (diff - days * (60 * 60 * 24) - hours * (60 * 60)) / 60; //分
                Long ss = (diff - days * (60 * 60 * 24) - hours * (60 * 60) - (minutes * 60));
                if (0 != days.longValue()) {
                    lastTime = "" + days + "天" + hours + "小时" + minutes + "分" + ss + "秒";
                    return lastTime;
                } else if (0 != hours.longValue()) {
                    lastTime = "" + hours + "小时" + minutes + "分" + ss + "秒";
                    return lastTime;
                } else if (0 != minutes.longValue()) {
                    lastTime = "" + minutes + "分" + ss + "秒";
                    return lastTime;
                } else if (0 != ss.longValue()) {
                    lastTime = "" + ss + "秒";
                    return lastTime;
                }
            }else {
                return "";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

}
