package com.example.edgecustomer.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.core.result.CommonCode;
import com.example.edgecustomer.core.result.Result;
import com.example.edgecustomer.core.result.ResultGenerator;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.model.dto.CustomZip;
import com.example.edgecustomer.service.*;
import com.example.edgecustomer.utils.FileOperater;
import com.example.edgecustomer.utils.JWTUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
public class CustomerController {
    @Autowired
    CustomerService customerService;
    @Autowired
    SystemService systemService;
    @Autowired
    UserService userService;
    @Autowired
    CompileService compileService;
    @Autowired
    RouterService routerService;
    @Autowired
    TemplateService templateService;

    @Value("${userPath}")
    private String userPath;

    @Value("${serverIp}")
    private  String serverIp;

    @PostMapping("/custom")
    public Result custom(String userName,@RequestParam("file") MultipartFile file) throws Exception {
        CustomZip customZip = compileService.parseCustomZip(file);
        String project = customZip.getSystemName();
        String customString = customZip.getCustomString();
        String routerString = customZip.getRouterString();
        JSONObject customJson;
        try{
            customJson = JSONObject.parseObject(customString);
        }catch (Exception e){
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,e.toString(),"The file formot of custom.json is incorrect");
        }
        JSONObject sceneJson = customJson.getJSONObject("scene");
        String core;
        if(sceneJson==null||!sceneJson.containsKey("core") || sceneJson.getString("core").equals("docker-compose")){
            core = "docker-compose";
        }else if(sceneJson.getString("core").equals("kubernetes")){
            core = "kubernetes";
        }else{
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,"An illegal core of "+ sceneJson.getString("core")+ " was set, tinyedge currently supports cores of docker-compose and kubernetes");
        }
        try{
            JSONObject moduleList = customerService.init(userName,customZip.getSystemName(),customString);
            customerService.handle();
        }catch (Exception e){
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,e.toString(),e.getMessage());
        }
        String workPath = userPath + "/" + userName +"/" + customZip.getSystemName();
        FileOperater.writeFile(customString,workPath+"/custom.json");

        //路由定制
        if(routerString!=null){
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
        }
        // 生成配置文件
        if(core.equals("docker-compose")){
            FileOperater.writeFile(customJson.toJSONString(),workPath+"/custom.json");
        }else if(core.equals("kubernetes")){
            String content = templateService.getTemplate("configMap");
            content = content.replace("#configContent",customJson.toJSONString());
            FileOperater.writeFile(content,workPath + "/config.yaml");
        }
        //命令生成
        String cmdStr = "";
        if(core.equals("docker-compose")){
            cmdStr = "curl -o system.zip '"  + serverIp + "/api/system/linklab/download?userName="+userName+"&systemName=" + project  + "' &&unzip system.zip&&rm system.zip&&cd system&&sudo docker-compose up -d";
            //String cmdStr = "curl -o system.zip -H Authorization:" + token + " \"" + backend_ip + "/api/system/downloadSystem?systemName=" + project +"&deviceName="+ deviceName + "\"&&unzip system.zip&&rm system.zip&&cd system&&sudo docker-compose build&&sudo docker-compose up"
        } else{
            cmdStr = "curl -o system.zip '"  + serverIp + "/api/system/linklab/download?userName="+userName+"&systemName=" + project  + "' &&unzip system.zip&&rm system.zip&&cd system";
        }

        return ResultGenerator.genSuccessResult(cmdStr);
    }

    // old interface
    @PostMapping("/oldcustom")
    @RequiresAuthentication
    public Result preprocess(HttpServletRequest request,String project,@RequestParam("file") MultipartFile file) throws IOException {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        //String username="zyx";
        //String jsonStr=FileOperater.readFile(jsonFilePath);
        String workPath = userPath + "/" + username +"/" + project;
        File tempfile = new File(workPath);
        if(tempfile.exists()){
            FileOperater.deleteDir(workPath);
        }
        tempfile.mkdirs();
        byte[] bytes = file.getBytes();
        Path path = Paths.get(workPath +"/custom.json");
        Files.write(path, bytes);
        String jsonFilePath = path.toString();
        String jsonStr=FileOperater.readFile(jsonFilePath);
        JSONObject moduleList;
        try{
            moduleList = customerService.init(username,project,jsonStr);
            customerService.handle();
        }catch (Exception e){
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,e.toString(),e.toString());
        }
        //String jsonStr="{\"serviceList\":[\"mysql\",\"deviceManagement\",\"authentication\"],\"serviceRelation\":[\"deviceManagement~authentication\"],\"serviceConfig\":{\"mysql\":{\"host\":\"edge-mysql\",\"port\":3306,\"user\":\"edge\",\"database\":\"edge_device\",\"password\":\"emnetsEdge301\"}}}";

        //List<String> serviceList =json.getJSONArray("serviceList").toJavaList(String.class);try{

            //Map services=(Map)yamlMap.get("services");


        /*String zipPath = workPath+"/system.zip";
        String sourceFile = "/edge-system/user/zyx/dfdf/output";
        //这个是压缩之后的文件绝对路径
        FileOutputStream fos = new FileOutputStream(zipPath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(outputPath);

        FileOperater.zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();*/
        System.out.println(moduleList);
        System.out.println("success");
        systemService.updateStatus( username, project,"已定制");
        //JSONObject jsonObject = JSONObject.parseObject(moduleList);
        return ResultGenerator.genSuccessResult(moduleList);
    }
}
