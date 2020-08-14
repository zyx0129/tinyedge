package com.example.edgecustomer.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.core.result.Result;
import com.example.edgecustomer.core.result.ResultGenerator;
import com.example.edgecustomer.model.System;
import com.example.edgecustomer.service.SystemService;
import com.example.edgecustomer.utils.FileOperater;
import com.example.edgecustomer.utils.JWTUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.ZipOutputStream;

@CrossOrigin
@RestController
public class SystemController {
    @Value("${userPath}")
    private String userPath;

    @Autowired
    SystemService systemService;

    @PostMapping("/system/create")
    @RequiresAuthentication
    public Result createSystem(HttpServletRequest request, String systemName, String desp) {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        systemService.createSystem(username, systemName, desp);
        List<System> systemList = systemService.listSystems(username);
        return ResultGenerator.genSuccessResult(systemList);
        // return ResultGenerator.genSuccessResult("success");
    }

    @PostMapping("/system/delete")
    @RequiresAuthentication
    public Result deleteSystem(HttpServletRequest request, Integer systemId, String systemName) throws IOException, NoSuchAlgorithmException {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        systemService.deleteSystem(systemId,systemName,username);
        return ResultGenerator.genSuccessResult("success");
    }

    @GetMapping("/system/list")
    @RequiresAuthentication
    public Result listSystems(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        List<System> systemList = systemService.listSystems(username);
        return ResultGenerator.genSuccessResult(systemList);
    }

    @GetMapping("/system/status")
    @RequiresAuthentication
    public Result checkStatus(HttpServletRequest request,String systemName) {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        String status = systemService.checkStatus(username,systemName);
        return ResultGenerator.genSuccessResult(status);
    }

    @GetMapping("/system/moduleList")
    @RequiresAuthentication
    public Result getModuleList(HttpServletRequest request,String systemName) {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        JSONObject moduleList= systemService.getModuleList(username,systemName);
        return ResultGenerator.genSuccessResult(moduleList);
    }



    @PostMapping("/system/download")
    //@RequiresAuthentication
    public Result downloadSystems(HttpServletRequest request, String systemName, HttpServletResponse response)  {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        String fileName = "system.zip";
        String filePath = userPath + "/" + username + "/" + systemName + "/" + fileName;
       /* File file = new File(filePath);
        filePath = file.getAbsolutePath();*/
        String url = "/download?filePath="+filePath;
        return ResultGenerator.genSuccessResult(url);
    }

    @GetMapping("/downloadSystem")
    @RequiresAuthentication
    public void download(HttpServletRequest request, HttpServletResponse response, String systemName, String deviceName) throws IOException {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        systemService.download(username,systemName,deviceName,response);
    }



        @GetMapping("/download")
    //@RequiresAuthentication
    public void downloadSystem(  HttpServletResponse response,String filePath)  {
        //filePath = "/edge-system/user/zyx/test/system.zip";

        File file = new File(filePath);
        filePath = file.getAbsolutePath();
        String fileName = file.getName();
        //下载的文件携带这个名称
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        //文件下载类型--二进制文件
        response.setContentType("application/octet-stream");

        try {
            FileInputStream fis = new FileInputStream(filePath);
            byte[] content = new byte[fis.available()];
            fis.read(content);
            fis.close();
            ServletOutputStream sos = response.getOutputStream();
            sos.write(content);
            sos.flush();
            sos.close();
        } catch (Exception e) {
            e.printStackTrace();
            //return ResultGenerator.genFailResult(CommonCode.SYSTEM_ERROR,"下载失败！");
        }

    }

 /*   @GetMapping("/zipFile")
    public void zipFile() throws IOException {
        //这个是文件夹的绝对路径，如果想要相对路径就自行了解写法
        String sourceFile = "/edge-system/user/zyx/dfdf/output";
        //这个是压缩之后的文件绝对路径
        FileOutputStream fos = new FileOutputStream(
                "/edge-system/user/zyx/dfdf/system.zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);

        FileOperater.zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }*/
}
