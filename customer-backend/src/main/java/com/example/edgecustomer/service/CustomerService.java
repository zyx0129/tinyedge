package com.example.edgecustomer.service;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.utils.CompileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface CustomerService {
    public JSONObject init(String userName, String systemName, String jsonStr) throws FileNotFoundException, CompileException;
    public void traversal(String libPath,String relativePath) throws Exception;
    public String preprocess(String str) throws Exception;
    public void handle() throws Exception;
}
