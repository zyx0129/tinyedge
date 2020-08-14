package com.example.edgecustomer.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.mapper.SystemMapper;
import com.example.edgecustomer.model.System;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.service.FunctionService;
import com.example.edgecustomer.service.ModuleService;
import com.example.edgecustomer.service.SystemService;
import com.example.edgecustomer.service.UserService;
import com.example.edgecustomer.utils.FileOperater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.zip.ZipOutputStream;

@Service
public class SystemServiceImpl implements SystemService {
    @Autowired
    SystemMapper systemMapper;
    @Autowired
    FunctionService functionService;
    @Autowired
    ModuleService moduleService;
    @Autowired
    UserService userService;

    @Value("${userPath}")
    private String userPath;

    @Override
    public void createSystem(String userName, String systemName,String desp){
        //UserService userService = new UserServiceImpl();
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        System system = new System();
        system.setUserId(userId);
        system.setName(systemName);
        system.setDescription(desp);
        system.setStatus("未定制");
        systemMapper.insert(system);
    }

    @Override
    public System getSystem(String userName, String systemName) {
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        Example example=new Example(System.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",systemName);
        System system = systemMapper.selectOneByExample(example);
        if(system== null){
            System newSystem = new System();
            newSystem.setUserId(userId);
            newSystem.setName(systemName);
            newSystem.setDescription("linklab system");
            newSystem.setStatus("已定制");
            systemMapper.insert(newSystem);
            return newSystem;
        }
        return  system;
    }


    @Override
    public void deleteSystem(Integer systemId,String systemName,String userName){
        systemMapper.deleteByPrimaryKey(systemId);
        String systemFilePath = userPath + "/" + userName + "/" + systemName;
        FileOperater.deleteDir(systemFilePath);
    }

    @Override
    public List<System> listSystems(String userName){
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        Example example=new Example(System.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        List<System> systemList = systemMapper.selectByExample(example);
       return systemList;
    }

    @Override
    public void updateStatus(String userName, String systemName,String status){
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        Example example=new Example(System.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",systemName);
        System system = new System();
        system.setStatus(status);
        systemMapper.updateByExampleSelective(system,example);
    }

    @Override
    public String checkStatus(String userName,String systemName){
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        Example example=new Example(System.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",systemName);
        String status = systemMapper.selectOneByExample(example).getStatus();
        return  status;
    }

    @Override
    public JSONObject getModuleList(String userName,String systemName){
        String jsonFilePath = userPath + "/" + userName +"/" + systemName + "/custom.json";
        String jsonStr= FileOperater.readFile(jsonFilePath);

        //String jsonStr="{\"serviceList\":[\"mysql\",\"deviceManagement\",\"authentication\"],\"serviceRelation\":[\"deviceManagement~authentication\"],\"serviceConfig\":{\"mysql\":{\"host\":\"edge-mysql\",\"port\":3306,\"user\":\"edge\",\"database\":\"edge_device\",\"password\":\"emnetsEdge301\"}}}";
        JSONObject customJson=JSONObject.parseObject(jsonStr);
        JSONObject moduleInfoJson = customJson.getJSONObject("serviceInfo");
        Set<String> serviceList = moduleInfoJson.keySet();
        Map map = new HashMap();
        List<String> systemList = new ArrayList<>();
        List<String> startList = new ArrayList<>();
        List<String> middleList = new ArrayList<>();
        List<String> endList = new ArrayList<>();
        List<String> aloneList = new ArrayList<>();
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        for (String service : serviceList) {
            String source = moduleInfoJson.getString(service);
            String type ="";
            try {
                if (source.equals("system")) {
                    type = moduleService.getModuleType(service);
                } else {
                    type = functionService.getModuleType(service, userId);
                }
            }catch(Exception e) {
                java.lang.System.out.println("No such service "+service);
            }
            switch (type) {
                case "system":
                    systemList.add(service);
                    break;
                case "start":
                    startList.add(service);
                    break;
                case "middle":
                    middleList.add(service);
                    break;
                case "end":
                    endList.add(service);
                    break;
                case "alone":
                    aloneList.add(service);
                    break;
            }
        }
        map.put("system",systemList);
        map.put("start",startList);
        map.put("middle",middleList);
        map.put("end",endList);
        map.put("alone",aloneList);
        JSONObject moduleList = new JSONObject(map);
        return moduleList;
    }

    @Override
    public void download(String userName, String systemName, String deviceName, HttpServletResponse response) throws IOException {
        String systemDir = userPath + "/" + userName + "/" + systemName;
        String fileName = deviceName+".zip";
        String zipPath = systemDir + "/"+fileName;
        //String sourceFile = "/edge-system/user/zyx/dfdf/output";
        //这个是压缩之后的文件绝对路径
        FileOutputStream fos = new FileOutputStream(zipPath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(systemDir + "/system");
        File customFile = new File(systemDir + "/"+deviceName+"_custom.json");
        if(!customFile.exists()){
            customFile = new File(systemDir + "/custom.json");
        }
        //File customFile = new File(systemDir + "/custom.json");
        FileOperater.zipFile(fileToZip, fileToZip.getName(), zipOut);
        FileOperater.zipFile(customFile, "system/config/custom.json", zipOut);
        File configYamlFile = new File(systemDir + "/config.yaml");
        if(configYamlFile.exists()){
            FileOperater.zipFile(configYamlFile, "system/config/config.yaml", zipOut);
        }
        File deviceInfo = new File(systemDir + "/deviceInfo.json");
        if(deviceInfo.exists()){
            FileOperater.zipFile(deviceInfo, "system/config/deviceInfo.json", zipOut);
        }
        zipOut.close();
        fos.close();

        //String fileName = "system.zip";
        //String filePath = userPath + "/" + username + "/" + systemName + "/device/"+ deviceName+"/"+fileName;
        File file = new File(zipPath);
        zipPath = file.getAbsolutePath();
        //下载的文件携带这个名称
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        //文件下载类型--二进制文件
        response.setContentType("application/octet-stream");

        try {
            FileInputStream fis = new FileInputStream(zipPath);
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



}
