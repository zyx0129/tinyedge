package com.example.edgecustomer.service.impl;

import com.example.edgecustomer.mapper.ModuleMapper;
import com.example.edgecustomer.mapper.TemplateMapper;
import com.example.edgecustomer.model.Module;
import com.example.edgecustomer.model.Template;
import com.example.edgecustomer.model.dto.PubConfig;
import com.example.edgecustomer.service.ModuleService;
import com.example.edgecustomer.service.TemplateService;
import com.example.edgecustomer.service.UserService;
import com.example.edgecustomer.utils.CompileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

@Service
public class TemplateServiceImpl implements TemplateService {
    @Autowired
    TemplateMapper templateMapper;

    @Override
    public String getTemplate(String templateName){
        Example example=new Example(Template.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name",templateName);
        Template template = templateMapper.selectOneByExample(example);
        if(template==null){
            return null;
        }
        return template.getContent();
    }

    @Override
    public void saveTemplate(String templateName, String content){
        Example example=new Example(Template.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name",templateName);
        Template template = templateMapper.selectOneByExample(example);
        if(template!=null){
            template.setContent(content);
            templateMapper.updateByPrimaryKey(template);
        }else{
            template = new Template();
            template.setName(templateName);
            template.setContent(content);
            templateMapper.insert(template);
        }
    }



}
