package com.example.edgecustomer.service;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.model.System;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface SystemService {
    public void createSystem(String userName, String systemName,String desp);
    public System getSystem(String userName, String systemName);
    public List<System> listSystems(String userName);
    public void updateStatus(String userName, String systemName,String status);
    public String checkStatus(String userName,String systemName);
    public JSONObject getModuleList(String userName, String systemName);
    public void deleteSystem(Integer systemId,String systemName,String userName);
    public void download(String userName, String systemName, String deviceName, HttpServletResponse response) throws IOException;
}
