package com.example.edgecustomer.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.mapper.UserMapper;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.model.dto.CustomZip;
import com.example.edgecustomer.service.CompileService;
import com.example.edgecustomer.service.DiyService;
import com.example.edgecustomer.service.FunctionService;
import com.example.edgecustomer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CompileServiceImpl implements CompileService {

    @Autowired
    FunctionService functionService;
    @Autowired
    DiyService diyService;

    //压缩包文件解析
    @Override
    public CustomZip parseCustomZip(MultipartFile file) throws Exception {
        ZipInputStream zip = new ZipInputStream(file.getInputStream());
        ZipEntry ze;
        CustomZip customZip = new CustomZip();
        List<String> validFiles = new ArrayList<>();
        validFiles.add("custom.json");
        validFiles.add("router.json");
        while ((ze = zip.getNextEntry()) != null) {
            String content = "";
            String line;
            String filePath = ze.getName();
            if (!ze.isDirectory()) {
                String[] strs = filePath.split("/");
                customZip.setSystemName(strs[0]);
                String fileName = strs[strs.length - 1];
                if (fileName.isEmpty() || !validFiles.contains(fileName)) continue;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zip));
                while ((line = bufferedReader.readLine()) != null) {
                    content += line + "\n";
                }
                if (fileName.equals("custom.json")) {
                    customZip.setCustomString(content);
                } else if (fileName.equals("router.json")) {
                    customZip.setRouterString(content);
                }
            }
        }
        return customZip;
    }

    @Override
    public String getCustom(MultipartFile file) throws IOException {
        ZipInputStream zip = new ZipInputStream(file.getInputStream());
        ZipEntry ze;
        String content="";
        while((ze =zip.getNextEntry())!=null){
            String line;
            String fileName = ze.getName();
            if(fileName.equals("TinyEdge/Custom/custom.json")){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zip));
                while((line = bufferedReader.readLine())!= null){
                    //System.out.println(line);
                    content+=line;
                }
            }
        }
        return content;
    }

    @Override
    public String getRouter(MultipartFile file) throws IOException{
        ZipInputStream zip = new ZipInputStream(file.getInputStream());
        ZipEntry ze;
        String content="";
        while((ze =zip.getNextEntry())!=null){
            String line;
            String fileName = ze.getName();
            if(fileName.equals("TinyEdge/Development/Router/router.json")){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zip));
                while((line = bufferedReader.readLine())!= null){
                    //System.out.println(line);
                    content+=line;
                }
            }
        }
        return  content;
    }

    @Override
    public void handleFc(Integer userId,MultipartFile file) throws IOException{
        ZipInputStream zip = new ZipInputStream(file.getInputStream());
        ZipEntry ze;
        while((ze =zip.getNextEntry())!=null){
            String content="";
            String line;
            String fileName = ze.getName();
            if(!ze.isDirectory() && fileName.contains("TinyEdge/Development/FC/")){
                String[] strs= fileName.split("/");
                String funcName = strs[strs.length-1].split("\\.")[0];
                if(funcName.isEmpty()) continue;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zip));
                while((line = bufferedReader.readLine())!= null){
                    //System.out.println(line);
                    content+=line+"\n";
                }
                functionService.handleFunction(userId,funcName);
                functionService.updateFunction(userId,funcName,content);
            }
        }
    }

    @Override
    public void handleDiy(Integer userId,MultipartFile file) throws IOException{
        ZipInputStream zip = new ZipInputStream(file.getInputStream());
        ZipEntry ze;
        while((ze =zip.getNextEntry())!=null){
            String content="";
            String line;
            String filePath = ze.getName();
            if(!ze.isDirectory() && filePath.contains("TinyEdge/Development/Diy/")){
                String[] strs= filePath.split("/");
                String fileName = strs[strs.length-1];
                String diyName = "";
                if(fileName.isEmpty()) continue;
                else if(fileName.equals("docker-compose.yaml") ||fileName.equals("k8s.yaml")){
                    diyName = strs[strs.length-2];
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zip));
                while((line = bufferedReader.readLine())!= null){
                    //System.out.println(line);
                    content+=line+"\n";
                }
               /* diyService.handleDiy(userId,diyName);
                if(fileName.equals("docker-compose.yaml")){
                    diyService.updateComposeFile(userId,diyName,content);
                }else if(fileName.equals("k8s.yaml")){
                    diyService.updateK8sFile(userId,diyName,content);
                }*/
                /*functionService.handleFunction(userName,funcName);
                functionService.updateFunction(userName,funcName,content);*/
            }
        }
    }



}
