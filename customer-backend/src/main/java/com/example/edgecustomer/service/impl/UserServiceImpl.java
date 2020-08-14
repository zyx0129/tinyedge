package com.example.edgecustomer.service.impl;

import com.example.edgecustomer.mapper.UserMapper;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.genid.GenId;

import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public User getUser(String username){
        Example example=new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username",username);
        User user = userMapper.selectOneByExample(example);
        if(user== null){
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword("pwd");
            newUser.setRole("user");
            userMapper.insert(newUser);
            return newUser;
        }
        /*List<User> userInfo = userMapper.selectByExample(example);
        if(userInfo.isEmpty()){
            return null;
        }
        User user = userInfo.get(0);*/

        return user;
    }

    @Override
    public void register(String username,String password){
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("user");
        userMapper.insert(user);
    }

}
