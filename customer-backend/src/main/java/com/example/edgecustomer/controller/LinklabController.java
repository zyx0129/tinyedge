package com.example.edgecustomer.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.core.result.CommonCode;
import com.example.edgecustomer.core.result.Result;
import com.example.edgecustomer.core.result.ResultGenerator;
import com.example.edgecustomer.model.System;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.service.*;
import com.example.edgecustomer.utils.FileOperater;
import com.example.edgecustomer.utils.JWTUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


@RestController
public class LinklabController {
    @Autowired
    CompileService compileService;
    @Autowired
    CustomerService customerService;
    @Autowired
    RouterService routerService;
    @Autowired
    SystemService systemService;
    @Autowired
    DeviceService deviceService;
    @Autowired
    UserService userService;

    @Value("${userPath}")
    private String userPath;
    @Value("${serverIp}")
    private  String serverIp;
    /*@Value("${jsonFilePath}")
    private String jsonFilePath;*/
    @Value("${fileLibPath}")
    private String fileLibPath;

    @PostMapping("/linklab/compile")
    public Result compile(String userName,@RequestParam("file") MultipartFile file) throws IOException, NoSuchAlgorithmException {
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        //fc处理
        //compileService.handleFc(userId,file);
        //diy应用处理
        //compileService.handleDiy(userId,file);

        //系统定制
        String customString = compileService.getCustom(file);
        String project = "linklab";
        String workPath = userPath + "/" + userName +"/" + project;

        JSONObject customJson;
        try{
            customJson = JSONObject.parseObject(customString);
        }catch (Exception e){
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,e.toString(),"The file formot of custom.json is incorrect");
        }
        JSONObject sceneJson = customJson.getJSONObject("scene");
        String core = "docker-compose";
        if(!customJson.containsKey("scene")||!sceneJson.containsKey("core") || sceneJson.getString("core").equals("docker-compose")){
            core = "docker-compose";
        }else if(sceneJson.getString("core").equals("kubernetes")){
            core = "kubernetes";
        }else{
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,"An illegal core of "+ sceneJson.getString("core")+ " was set, tinyedge currently supports cores of docker-compose and kubernetes");
        }
        try{
            JSONObject moduleList = customerService.init(userName,project,customString);
            customerService.handle();
        }catch (Exception e){
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,e.toString(),e.getMessage());
        }
        //路由定制
        String routerString = compileService.getRouter(file);
        FileOperater.writeFile(customString,workPath+"/custom.json");
        try{
             JSONObject router = JSONObject.parseObject(routerString);
        }catch (Exception e){
             return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,e.toString(),"router.json 格式错误");
        }
        String pubSubStr = routerService.generateRouter(routerString);
        if(!pubSubStr.isEmpty()){
            JSONObject pubSubObj = JSONObject.parseObject(pubSubStr);
            customJson.put("router",pubSubObj);
        }
        // 生成配置文件
        if(core.equals("docker-compose")){
            FileOperater.writeFile(customJson.toJSONString(),workPath+"/custom.json");
        }else if(core.equals("kubernetes")){
            String content = FileOperater.readFile(fileLibPath+"/config.yaml");
            content = content.replace("#configContent",customJson.toJSONString());
            FileOperater.writeFile(content,workPath + "/config.yaml");
        }
        //设备处理
        System system = systemService.getSystem(userName,project);
        Integer systemId = system.getId();
        String deviceName = "linklab_device";
        deviceService.handleDevice(systemId,deviceName,userName,project);
        //命令生成
        String cmdStr = "";
        if(core.equals("docker-compose")){
            cmdStr = "curl -o system.zip '"  + serverIp + ":12381/api/system/linklab/download?userName="+userName+"&systemName=" + project  + "' &&unzip system.zip&&rm system.zip&&cd system&&sudo docker-compose up -d";
            //String cmdStr = "curl -o system.zip -H Authorization:" + token + " \"" + backend_ip + "/api/system/downloadSystem?systemName=" + project +"&deviceName="+ deviceName + "\"&&unzip system.zip&&rm system.zip&&cd system&&sudo docker-compose build&&sudo docker-compose up"
        } else{
            cmdStr = "curl -o system.zip '"  + serverIp + ":12381/api/system/linklab/download?userName="+userName+"&systemName=" + project  + "' &&unzip system.zip&&rm system.zip&&cd system";
        }
        /*System.out.println(moduleList);
        System.out.println("success");
        systemService.updateStatus( username, project,"已定制");*/
        //JSONObject jsonObject = JSONObject.parseObject(moduleList);
        return ResultGenerator.genSuccessResult(cmdStr);
    }

    @GetMapping("/linklab/download")
    public void linklabDownload(HttpServletResponse response, String userName, String systemName) throws IOException {
        systemService.download(userName,systemName,"linklab",response);
    }
}
