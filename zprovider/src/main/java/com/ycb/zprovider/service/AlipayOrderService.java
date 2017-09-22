package com.ycb.zprovider.service;

import com.ycb.zprovider.cache.RedisService;
import com.ycb.zprovider.mapper.*;
import com.ycb.zprovider.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Huo on 2017/9/22.
 */
@Service
public class AlipayOrderService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private ShopStationMapper shopStationMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 创建未付费订单的方法
     * 当用户扫码后，就生成预付费订单，然后再向支付宝申请生成支付宝的信用借还订单
     * @param outOrderNo    商户的订单编号
     * @param sid   设备的编号
     * @param cableType 充电宝线的类型
     * @param session   用户的标识，在redis中的key,用于从redis中获取到用户的编号
     */
    public void createPreOrder(String outOrderNo, String sid, String cableType, String session) {
        //根据session从redis中查询用户的open_id
        String openid = redisService.getKeyValue(session);
        User user = this.userMapper.findUserinfoByOpenid(openid);

        //向ycb_mcs_tradelog中存入数据
        Order order = new Order();
        //设置用户编号
        order.setCustomer(user.getId());
        order.setCreatedBy("SYS:createcreditpay");
        order.setCreatedDate(new Date());
        //设置租借时间
        order.setBorrowTime(new Date());
        //设置订单编号，为商户自己的订单编号，格式为yyyyMMddHHmmss+4位随机数
        order.setOrderid(outOrderNo);//订单编号
        order.setPlatform(2);//平台(信用借还)
        //根据sid查询设备所在店铺的信息
        Shop responseShop = shopMapper.getShopInfoBySid(sid);
        order.setPrice(responseShop.getDefaultPay());//商品价格(元)，从数据库中查询押金defaultPay
        order.setPaid(BigDecimal.ZERO);//已支付的费用
        order.setUsefee(BigDecimal.ZERO);//产生的费用

        order.setCable(Integer.valueOf(cableType));
        order.setStatus(0);//支付状态,0为未支付，1为已经支付
        order.setBorrowShopId(responseShop.getId());
        //根据返回来的sid，查询到设备所在商铺的信息，和设备的信息
        ShopStation shopStation = shopStationMapper.findShopStationIdBySid(sid);
        Station station = stationMapper.getStationBySid(sid);
        order.setBorrowShopStationId(shopStation.getId());
        order.setBorrowStationId(station.getId());
        //设置租借地点
        order.setBorrowStationName(responseShop.getName());
        order.setBorrowCity(responseShop.getCity());
        orderMapper.saveOrder(order);
    }
}
