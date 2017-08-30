package com.ycb.wxxcx.provider.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by zhuhui on 17-8-16.
 */
@Service
public class SocketService {

    public static final Logger logger = LoggerFactory.getLogger(SocketService.class);

    @Value("${socketIp}")
    private String socketIp;

    @Value("${socketPort}")
    private Integer socketPort;

    public void SendCmd(String cmd) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(socketIp, socketPort);
            //向服务器端发送数据
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
            out.write(cmd);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } finally {
            socket.close();
            logger.info(cmd);
        }
    }
}
