package com.example.edgecustomer.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.mapper.FunctionMapper;
import com.example.edgecustomer.mapper.SystemMapper;
import com.example.edgecustomer.model.Function;
import com.example.edgecustomer.model.Module;
import com.example.edgecustomer.model.System;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.service.FunctionService;
import com.example.edgecustomer.service.SystemService;
import com.example.edgecustomer.service.TemplateService;
import com.example.edgecustomer.service.UserService;
import com.example.edgecustomer.utils.CompileException;
import com.example.edgecustomer.utils.FileOperater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FunctionServiceImpl implements FunctionService {
    @Autowired
    FunctionMapper functionMapper;
    @Autowired
    UserService userService;
    @Autowired
    TemplateService templateService;

    @Value("${fileLibPath}")
    private String fileLibPath;
    @Value("${userPath}")
    private String userPath;

    @Override
    public void saveFunction(Integer userId, MultipartFile file) throws CompileException, IOException {
        ZipInputStream zip = new ZipInputStream(file.getInputStream());
        ZipEntry ze;
        String functionName = "";
        String fcContent = "";
        String pubConfigStr = "";
        List<String> validFiles = new ArrayList<>();
        validFiles.add("msgRec.py");
        validFiles.add("pubConfig.json");
        while((ze =zip.getNextEntry())!=null){
            String content="";
            String line;
            String filePath = ze.getName();
            if(!ze.isDirectory()){
                String[] strs= filePath.split("/");
                functionName = strs[0];
                String fileName = strs[strs.length-1];
                if(fileName.isEmpty() || !validFiles.contains(fileName)) continue;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zip));
                while((line = bufferedReader.readLine())!= null){
                    content+=line+"\n";
                }
                if(fileName.equals("msgRec.py")){
                    fcContent = content;
                }else if(fileName.equals("pubConfig.json")){
                    pubConfigStr = content;
                }
            }
        }
        Example example=new Example(Function.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",functionName);
        Function function = functionMapper.selectOneByExample(example);
        if(function!=null){
            function.setContent(fcContent);
            functionMapper.updateByPrimaryKey(function);
        }else{  //新插入的需要预处理compose 和k8s 文件再保存
            function = new Function();
            function.setName(functionName);
            function.setUserId(userId);
            function.setContent(fcContent);
            function.setType("IFTTT");
            function.setStatus("已完成");
            String pythonFcCompose = templateService.getTemplate("pythonFcCompose");
            if(pythonFcCompose==null){
                throw new CompileException("python fc docker-compose.yaml template does not exist");
            }
            pythonFcCompose = pythonFcCompose.replace("#serviceName",functionName);
            function.setCompose(pythonFcCompose);
            String pythonFcKubernetes = templateService.getTemplate("pythonFcKubernetes");
            if(pythonFcKubernetes==null){
                throw new CompileException("python fc k8s.yaml template does not exist");
            }
            pythonFcKubernetes = pythonFcKubernetes.replace("#serviceName",functionName);
            function.setKubernetes(pythonFcKubernetes);
            functionMapper.insert(function);
        }
    }

    @Override
    public void createFunction(Integer userId, String functionName, String desp) {
        Function function = new Function();
        function.setUserId(userId);
        function.setName(functionName);
        function.setDescription(desp);
        function.setType("end");
        function.setStatus("待开发");
        functionMapper.insert(function);
    }

    @Override
    public void handleFunction(Integer userId, String functionName) {
        Example example=new Example(Function.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",functionName);
        Function function = functionMapper.selectOneByExample(example);
        if(function==null){
            createFunction(userId,functionName,functionName);
        }
    }



        @Override
    public void deleteFunction(String userName, String functionName) {
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        Example example = new Example(Function.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", userId);
        criteria.andEqualTo("name", functionName);
        functionMapper.deleteByExample(example);
        //functionMapper.deleteByPrimaryKey(functionId);
        String fcPath = userPath+ "/" + userName +"/userLib/"+functionName;
        FileOperater.deleteDir(fcPath);
    }

    @Override
    public List<Function> listFunctions(String userName) {
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        Example example = new Example(Function.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", userId);
        List<Function> functionList = functionMapper.selectByExample(example);
        return functionList;
    }

    @Override
    public void updateStatus(Integer userId, String functionName,String status){
        Example example=new Example(Function.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",functionName);
        Function function = new Function();
        function.setStatus(status);
        functionMapper.updateByExampleSelective(function,example);
    }

    public void preprocess(String functionName,String filePath){
        String content = FileOperater.readFile(filePath);
        content = content.replace("#serviceName",functionName);
        FileOperater.writeFile(content,filePath);
    }

    @Override
    public void updateFunction(Integer userId,String functionName,String fcStr) throws IOException {
        //更新compose文件
        Example example=new Example(Function.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",functionName);
        Function function = new Function();
        function.setContent(fcStr);
        functionMapper.updateByExampleSelective(function,example);
       /* String composeTemplate = fileLibPath+"/template/docker-compose.yaml";
        String content = FileOperater.readFile(composeTemplate);
        content = content.replace("#serviceName",functionName);
        String newYamlPath = fcDirPath+"/docker-compose.yaml";
        FileOperater.writeFile(content,newYamlPath);
        //更新k8s文件
        String k8sTemplate = fileLibPath+"/template/k8s.yaml";
        content = FileOperater.readFile(k8sTemplate);
        content = content.replace("#serviceName",functionName);
        newYamlPath = fcDirPath+"/k8s.yaml";
        FileOperater.writeFile(content,newYamlPath);
        //更新msgRec文件
        String fcPath = fcDirPath + "/msgRec.py";
        FileOperater.writeFile(fcStr,fcPath);*/

        updateStatus(userId,functionName,"已发布");
    }


    @Override
    public String checkStatus(String userName,String functionName){
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        Example example=new Example(Function.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",functionName);
        String status = functionMapper.selectOneByExample(example).getStatus();
        return  status;
    }

    @Override
    public String getModuleType(String moduleName,Integer userId){
        Example example=new Example(Function.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",moduleName);
        Function function = functionMapper.selectOneByExample(example);
        if(function==null){
            return null;
        }
        return  function.getType();
    }

    @Override
    public Function getFunction(Integer userId,String moduleName){
        Example example=new Example(Function.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",moduleName);
        Function function = functionMapper.selectOneByExample(example);
        return function;
    }





}
