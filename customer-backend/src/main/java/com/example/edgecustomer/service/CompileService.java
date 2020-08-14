package com.example.edgecustomer.service;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.model.System;
import com.example.edgecustomer.model.dto.CustomZip;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CompileService {
    public CustomZip parseCustomZip(MultipartFile file) throws Exception;
    public String getCustom(MultipartFile file) throws IOException;
    public String getRouter(MultipartFile file) throws IOException;
    public void handleFc(Integer userId,MultipartFile file) throws IOException;
    public void handleDiy(Integer userId,MultipartFile file) throws IOException;
}
