package com.example.edgecustomer.service;


import com.example.edgecustomer.model.Diy;
import com.example.edgecustomer.utils.CompileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DiyService {
    public void saveDiy(Integer userId, MultipartFile file) throws IOException, CompileException;
    public String getModuleType(String moduleName, Integer userId);
    public void handleDiy(String userName, String diyName);
    public void updateComposeFile(String userName, String diyName, String content) throws IOException;
    public void updateK8sFile(String userName, String diyName, String content) throws IOException;
    public void createDiy(String userName, String diyName, String desp);
    public void updateStatus(String userName, String diyName, String status);
    public Diy getDiy(Integer userId,String diyName);
}
