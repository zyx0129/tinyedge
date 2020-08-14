package com.example.edgecustomer.controller;

import com.example.edgecustomer.core.result.CommonCode;
import com.example.edgecustomer.core.result.Result;
import com.example.edgecustomer.core.result.ResultGenerator;
import com.example.edgecustomer.mapper.TemplateMapper;
import com.example.edgecustomer.model.Template;
import com.example.edgecustomer.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;

@CrossOrigin
@RestController
public class TemplateController {
    @Autowired
    TemplateService templateService;

    /**
     *  linklab 模板文件保存接口，只有管理员可以创建或更新
     * @param templateName
     * @param file
     * @return
     */
    @PostMapping("/template/save")
    //TODO 添加权限校验，只有管理员可以创建或更新模板
    public Result saveTemplate(String templateName,@RequestParam("file") MultipartFile file) throws IOException {
        String content = new String(file.getBytes());
        try{
            templateService.saveTemplate(templateName,content);
        }catch (Exception e){
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,e.toString());
        }
        return ResultGenerator.genSuccessResult("success");
    }
}
