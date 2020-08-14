package com.example.edgecustomer.service.impl;

import com.example.edgecustomer.mapper.FunctionMapper;
import com.example.edgecustomer.mapper.ModuleMapper;
import com.example.edgecustomer.model.Function;
import com.example.edgecustomer.model.Module;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.model.dto.PubConfig;
import com.example.edgecustomer.service.FunctionService;
import com.example.edgecustomer.service.ModuleService;
import com.example.edgecustomer.service.UserService;
import com.example.edgecustomer.utils.CompileException;
import com.example.edgecustomer.utils.FileOperater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class ModuleServiceImpl implements ModuleService {
    @Autowired
    ModuleMapper moduleMapper;
    @Autowired
    UserService userService;


    @Value("${userPath}")
    private String userPath;

    @Override
    public void saveModule(Integer userId, String compose, String kubernetes, PubConfig pubConfig, String core) throws CompileException {
        String newName = pubConfig.getNewName();
        Example example=new Example(Module.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name",newName);
        String architecture = pubConfig.getArchitecture().replace("[","").replace("]","")
                .replace("\"","").replace("'","").replace(" ","");
        Module module = moduleMapper.selectOneByExample(example);
        if(module!=null){
            Integer moduleUserId = module.getUserId();
            if(moduleUserId.equals(userId)){
                module.setUserId(userId);
                module.setName(newName);
                module.setCore(core);
                module.setCompose(compose);
                module.setKubernetes(kubernetes);
                module.setAuthor(pubConfig.getAuthor());
                module.setDescription(pubConfig.getDescription());
                module.setEmail(pubConfig.getEmail());
                module.setDocument(pubConfig.getDocument());
                module.setType(pubConfig.getType());
                module.setArchitecture(architecture);
                moduleMapper.updateByPrimaryKey(module);
            }else{
                throw new CompileException("Module name " + newName + " has been registered by other users");
            }
        }else{
            module = new Module();
            module.setUserId(userId);
            module.setName(newName);
            module.setCore(core);
            module.setCompose(compose);
            module.setKubernetes(kubernetes);
            module.setAuthor(pubConfig.getAuthor());
            module.setDescription(pubConfig.getDescription());
            module.setEmail(pubConfig.getEmail());
            module.setDocument(pubConfig.getDocument());
            module.setType(pubConfig.getType());
            module.setArchitecture(architecture);
            module.setStatus("已发布");
            moduleMapper.insert(module);
        }
    }

    @Override
    public Module getModule(String moduleName){
        Example example=new Example(Module.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name",moduleName);
        Module module = moduleMapper.selectOneByExample(example);
        return module;
    }

    @Override
    public String getModuleType(String moduleName){
        Example example=new Example(Module.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name",moduleName);
        Module module = moduleMapper.selectOneByExample(example);
        if(module==null){
            return null;
        }
        return  module.getType();
    }




}
