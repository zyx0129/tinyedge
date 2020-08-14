package com.example.edgecustomer.service;

import com.example.edgecustomer.utils.CompileException;

public interface TemplateService {
    public String getTemplate(String templateName);
    public void saveTemplate(String templateName, String content);
}
