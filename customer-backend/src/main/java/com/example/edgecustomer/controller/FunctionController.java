package com.example.edgecustomer.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.core.result.CommonCode;
import com.example.edgecustomer.core.result.Result;
import com.example.edgecustomer.core.result.ResultGenerator;
import com.example.edgecustomer.model.Function;
import com.example.edgecustomer.model.System;
import com.example.edgecustomer.model.Template;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.service.FunctionService;
import com.example.edgecustomer.service.UserService;
import com.example.edgecustomer.utils.FileOperater;
import com.example.edgecustomer.utils.JWTUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@CrossOrigin
@RestController
public class FunctionController {
    @Value("${userPath}")
    private String userPath;

    @Autowired
    FunctionService functionService;
    @Autowired
    UserService userService;

    /**
     * linklab 用户函数计算保存接口，新函数创建，已有函数保存
     * @param userName
     * @param functionName
     * @param file
     * @return
     */
    @PostMapping("/function/save")
    public Result saveFunction(String userName,@RequestParam("file") MultipartFile file) throws IOException {
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        //String content = new String(file.getBytes());
        try {
            functionService.saveFunction(userId,file);
        }catch (Exception e){
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,e.toString(),e.getMessage());
        }
        return ResultGenerator.genSuccessResult("success");
    }

    @PostMapping("/function/create")
    @RequiresAuthentication
    public Result createFunction(HttpServletRequest request, String functionName, String desp) {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        User user = userService.getUser(username);
        Integer userId = user.getId();
        String regex = "[a-z]+[a-z//-]+?[a-z]+";
        if(!functionName.matches(regex)){
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,"函数名错误，必须为小写字母与-的组合，且开头结尾不能为-");
        }
        functionService.createFunction(userId, functionName, desp);
        List<Function> functionList = functionService.listFunctions(username);
        return ResultGenerator.genSuccessResult(functionList);
        //return ResultGenerator.genSuccessResult("success");
    }

    @PostMapping("/function/delete")
    @RequiresAuthentication
    public Result deleteFunction(HttpServletRequest request, String functionName) throws IOException, NoSuchAlgorithmException {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        functionService.deleteFunction(username,functionName);
        return ResultGenerator.genSuccessResult("success");
    }

    @GetMapping("/function/list")
    @RequiresAuthentication
    public Result listFunctions(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        List<Function> functionList = functionService.listFunctions(username);
        return ResultGenerator.genSuccessResult(functionList);
    }

    @PostMapping("/function/update")
    @RequiresAuthentication
    public Result updateFunction(HttpServletRequest request, String functionName,@RequestParam("file") MultipartFile file)  throws IOException{
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        byte[] bytes = file.getBytes();
        String fcStr = new String(bytes);
        User user = userService.getUser(username);
        Integer userId = user.getId();
        functionService.updateFunction(userId,functionName,fcStr);
        List<Function> functionList = functionService.listFunctions(username);
        return ResultGenerator.genSuccessResult(functionList);
        //return ResultGenerator.genSuccessResult("success");
    }


    @GetMapping("/function/status")
    @RequiresAuthentication
    public Result checkStatus(HttpServletRequest request,String systemName) {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        String status = functionService.checkStatus(username,systemName);
        return ResultGenerator.genSuccessResult(status);
    }


}
