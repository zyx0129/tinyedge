package com.example.edgecustomer.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.core.result.CommonCode;
import com.example.edgecustomer.core.result.Result;
import com.example.edgecustomer.core.result.ResultGenerator;
import com.example.edgecustomer.model.Device;
import com.example.edgecustomer.model.Mqtt;
import com.example.edgecustomer.service.DeviceService;
import com.example.edgecustomer.service.RouterService;
import com.example.edgecustomer.service.SystemService;
import com.example.edgecustomer.utils.FileOperater;
import com.example.edgecustomer.utils.JWTUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public class RouterController {

    @Value("${userPath}")
    private String userPath;
    @Autowired
    RouterService routerService;
    @Autowired
    SystemService systemService;

    @PostMapping("router/generate")
    public Result generateRouter(@RequestParam("file") MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String jsonstr = new String(bytes);
        routerService.generateRouter(jsonstr);
        /*JSONObject json=JSONObject.parseObject(str);
        String deviceName = json.getString("deviceName");*/
        return ResultGenerator.genSuccessResult("success");
    }

    @RequiresAuthentication
    @PostMapping("router/custom")
    public Result customRouter(HttpServletRequest request,String project,@RequestParam("file") MultipartFile file) throws IOException {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        String status = systemService.checkStatus(username,project);
        if(!status.equals("已定制")){
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,"系统尚未定制成功");
        }
        String jsonFilePath = userPath + "/" + username +"/" + project+"/custom.json";
        String jsonStr= FileOperater.readFile(jsonFilePath);
        JSONObject customJson=JSONObject.parseObject(jsonStr);
        byte[] bytes = file.getBytes();
        String jsonstr = new String(bytes);
        String pubSubStr = routerService.generateRouter(jsonstr);
        JSONObject pubSubObj = JSONObject.parseObject(pubSubStr);
        customJson.put("router",pubSubObj);
        FileOperater.writeFile(customJson.toJSONString(),jsonFilePath);
        //拆开分发给每个module
        /*for (Map.Entry<String, Object> entry : pubSubObj.entrySet()) {
            String moduleName = entry.getKey();
            String routerPath = systemPath+"/"+moduleName+"/config/router.json";
            String psStr =entry.getValue().toString();
            FileOperater.writeFile(psStr,routerPath);
        }*/
        //FileOperater.writeFile(pubSubStr,routerPath);
        return ResultGenerator.genSuccessResult("success");
    }

}
