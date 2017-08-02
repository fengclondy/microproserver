package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.vo.Order;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

/**
 * Created by zhuhui on 17-6-19.
 */
@RestController
@RequestMapping("order")
public class OrderController {
    // 获取用户的订单记录 {user_id}
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public List<Order> query(@PathVariable Long id) {
        return null;
    }

    // 用户扫码下单
    @RequestMapping(value = "/borrowBattery", method = RequestMethod.GET)
    public String borrowBattery() throws IOException {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 8000);
            //向服务器端发送数据
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
            out.write("ACT:rent_confirm;STATUS:1;ORDERID:xxxx;ID:xxxx;STATIONID:xxxx;COLORID:xxxx;POWER:xxx;ISDAMAGE:xxxx;VOLTAGE:xxxx;ADAPTER:xxxx;CABLE:xxxx;SLOT:xxxx;TEMPERATURE:xxxx;BATT_TYPE:xxxx\r\n");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
        return null;
    }
    // 用户充电宝报失 根据用户id 更新订单状态
}
