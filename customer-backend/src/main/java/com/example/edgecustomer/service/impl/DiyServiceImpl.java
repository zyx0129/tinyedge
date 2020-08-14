package com.example.edgecustomer.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.mapper.DiyMapper;
import com.example.edgecustomer.mapper.FunctionMapper;
import com.example.edgecustomer.model.Diy;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.model.dto.PubConfig;
import com.example.edgecustomer.service.DiyService;
import com.example.edgecustomer.service.FunctionService;
import com.example.edgecustomer.service.ModuleService;
import com.example.edgecustomer.service.UserService;
import com.example.edgecustomer.utils.CompileException;
import com.example.edgecustomer.utils.FileOperater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DiyServiceImpl implements DiyService {
    @Autowired
    DiyMapper diyMapper;
    @Autowired
    UserService userService;
    @Autowired
    ModuleService moduleService;

    @Value("${fileLibPath}")
    private String fileLibPath;
    @Value("${userPath}")
    private String userPath;

    @Override
    public void saveDiy(Integer userId, MultipartFile file) throws IOException, CompileException {
        ZipInputStream zip = new ZipInputStream(file.getInputStream());
        ZipEntry ze;
        String compose = "";
        String kubernetes = "";
        String pubConfigStr = "";
        List<String> validFiles = new ArrayList<>();
        validFiles.add("docker-compose.yaml");
        validFiles.add("k8s.yaml");
        validFiles.add("pubConfig.json");
        String diyName = "";
        while((ze =zip.getNextEntry())!=null){
            String content="";
            String line;
            String filePath = ze.getName();
            if(!ze.isDirectory()){
                String[] strs= filePath.split("/");
                diyName = strs[0];
                String fileName = strs[strs.length-1];
                if(fileName.isEmpty() || !validFiles.contains(fileName)) continue;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zip));
                while((line = bufferedReader.readLine())!= null){
                    content+=line+"\n";
                }
                if(fileName.equals("docker-compose.yaml")){
                    compose = content;
                }else if(fileName.equals("k8s.yaml")){
                    kubernetes = content;
                }else if(fileName.equals("pubConfig.json")){
                    pubConfigStr = content;
                }
            }
        }
        Example example=new Example(Diy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",diyName);
        Diy diy = diyMapper.selectOneByExample(example);
        String core;
        if(!compose.isEmpty() && kubernetes.isEmpty()){
            core = "compose";
        }else if(compose.isEmpty() && !kubernetes.isEmpty()){
            core = "kubernetes";
        }else if(!compose.isEmpty() && !kubernetes.isEmpty()){
            core = "both";
        }else {
            throw new CompileException("No valid yaml file founded,diy image service must upload docker-compose.yaml or k8s.yaml");
        }
        if(diy!=null){
            diy.setCore(core);
            diy.setCompose(compose);
            diy.setKubernetes(kubernetes);
            diyMapper.updateByPrimaryKey(diy);
        }else{
            diy = new Diy();
            diy.setName(diyName);
            diy.setUserId(userId);
            diy.setType("alone");
            diy.setCore(core);
            diy.setCompose(compose);
            diy.setKubernetes(kubernetes);
            diyMapper.insert(diy);
        }
        //如果pubConfig不为空，执行发布流程
        PubConfig pubConfig = JSON.parseObject(pubConfigStr, PubConfig.class);
        if(pubConfig!=null){
            moduleService.saveModule(userId,compose,kubernetes,pubConfig,core);
        }
    }

    @Override
    public Diy getDiy(Integer userId,String diyName){
        Example example=new Example(Diy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",diyName);
        Diy diy = diyMapper.selectOneByExample(example);
        return diy;
    }



    @Override
    public void createDiy(String userName, String diyName, String desp) {
        //UserService userService = new UserServiceImpl();
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        Diy diy = new Diy();
        diy.setUserId(userId);
        diy.setName(diyName);
        diy.setDescription(desp);
        diy.setType("end");
        diyMapper.insert(diy);
    }

    @Override
    public void handleDiy(String userName, String diyName) {
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        Example example=new Example(Diy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",diyName);
        Diy diy = diyMapper.selectOneByExample(example);
        if(diy==null){
            createDiy(userName,diyName,diyName);
        }
    }



    @Override
    public void updateComposeFile(String userName,String diyName,String content) throws IOException {
        String fcDirPath = userPath+ "/" + userName +"/userLib/"+diyName;
        //FileOperater.deleteDir(fcDirPath);
        String newYamlPath = fcDirPath+"/docker-compose.yaml";
        FileOperater.writeFile(content,newYamlPath);
        //}
        updateStatus(userName,diyName,"已发布");
    }

    @Override
    public void updateK8sFile(String userName,String diyName,String content) throws IOException {
        String fcDirPath = userPath+ "/" + userName +"/userLib/"+diyName;
        //FileOperater.deleteDir(fcDirPath);
        String newYamlPath = fcDirPath+"/k8s.yaml";
        FileOperater.writeFile(content,newYamlPath);
        //}
        updateStatus(userName,diyName,"已发布");
    }

    @Override
    public void updateStatus(String userName, String functionName,String status){
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        Example example=new Example(Diy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",functionName);
        Diy diy = new Diy();
        diyMapper.updateByExampleSelective(diy,example);
    }




    @Override
    public String getModuleType(String moduleName,Integer userId){
        Example example=new Example(Diy.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("name",moduleName);
        String type = diyMapper.selectOneByExample(example).getType();
        return  type;
    }
}
