package com.example.edgecustomer.controller;

import com.example.edgecustomer.core.result.CommonCode;
import com.example.edgecustomer.core.result.Result;
import com.example.edgecustomer.core.result.ResultGenerator;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.service.UserService;
import com.example.edgecustomer.utils.JWTUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping("/login")
    public Result login( String username,String password) {
        User user = userService.getUser(username);
        if (user == null) {
            return ResultGenerator.genFailResult(CommonCode.USER_NOTEXIT);
        }
        if (user.getPassword().equals(password)) {
            return ResultGenerator.genSuccessResult(CommonCode.SUCCESS,"Login success", JWTUtil.sign(username, password));
        } else {
            return ResultGenerator.genFailResult(CommonCode.PASSWORD_WRONG);
        }
    }

    @PostMapping("/register")
    public Result register( String username,String password) {
        User user = userService.getUser(username);
        if (user != null) {
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,"user exist","");
        }
        userService.register(username,password);
        return  ResultGenerator.genSuccessResult("success");
    }

    @PostMapping("/decode")
    public Result decode(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        return ResultGenerator.genSuccessResult(username);
    }

    @GetMapping("/require_auth")
    @RequiresAuthentication
    public Result requireAuth(){
        return ResultGenerator.genSuccessResult("SUCCESS");    }
    }
