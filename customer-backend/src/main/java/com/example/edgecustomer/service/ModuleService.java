package com.example.edgecustomer.service;

import com.example.edgecustomer.model.Function;
import com.example.edgecustomer.model.Module;
import com.example.edgecustomer.model.dto.PubConfig;
import com.example.edgecustomer.utils.CompileException;

import java.io.IOException;
import java.util.List;

public interface ModuleService {
    public String getModuleType(String moduleName);
    public void saveModule(Integer userId, String compose, String kubernetes, PubConfig pubConfig,String core) throws CompileException;
    public Module getModule(String moduleName);
}
