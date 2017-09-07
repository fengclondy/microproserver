package com.ycb.wxxcx.provider.service;

import com.ycb.wxxcx.provider.mapper.FrequencyMapper;
import com.ycb.wxxcx.provider.mapper.OrderMapper;
import com.ycb.wxxcx.provider.vo.Frequency;
import com.ycb.wxxcx.provider.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by duxinyuan:检查租借频率 on 2017/9/1.
 */

@Service
public class FrequencyService {

    public static final Logger logger = LoggerFactory.getLogger(FrequencyService.class);

    @Autowired(required = false)
    private OrderMapper orderMapper;

    @Autowired(required = false)
    private FrequencyMapper frequencyMapper;

    // 检测用户租借频率
    public boolean queryBorrowFrequency(User user) {

        try {
            //查询限制条件
            Frequency frequency = this.frequencyMapper.findFrequency();
            //检索用户正在使用电池数量
            Integer useNum = this.orderMapper.findUserUseBatteryNum(user);
            if (useNum < frequency.getBatteryNum()){ //正在使用的电池数量小于3块
                //检索用户当日订单数量
                Integer orderNum = this.orderMapper.findUserOrderNum(user);
                if (orderNum < frequency.getOrderNum()){ //当日租借成功订单数量小于10个
                    //可以租借
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }
}