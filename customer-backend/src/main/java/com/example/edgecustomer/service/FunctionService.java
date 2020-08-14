package com.example.edgecustomer.service;

import com.example.edgecustomer.model.Function;
import com.example.edgecustomer.utils.CompileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FunctionService {
    public void saveFunction(Integer userId, MultipartFile file) throws CompileException, IOException;
    public void createFunction(Integer userId, String functionName, String desp);
    public List<Function> listFunctions(String userName);
    public void updateStatus(Integer userId, String functionName, String status);
    public String checkStatus(String userName, String functionName);
    public void deleteFunction(String userName,String functionName);
    public void updateFunction(Integer userId,String functionName,String content) throws IOException;
    public String getModuleType(String moduleName,Integer userId);
    public void handleFunction(Integer userId, String functionName);
    public Function getFunction(Integer userId,String functionName);
}
