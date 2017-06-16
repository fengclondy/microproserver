package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.mapper.UserMapper;
import com.ycb.wxxcx.provider.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by zhuhui on 17-6-16.
 */
@RestController
@RequestMapping("user")
public class UserController {
    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserMapper userMapper;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public User query(@PathVariable Long id) {
        return this.userMapper.findById(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    public int insert(@RequestBody User user) {
        return this.userMapper.insert(user.getName(), user.getAge());
    }
}
