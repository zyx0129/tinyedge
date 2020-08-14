package com.example.edgecustomer.controller;

import com.example.edgecustomer.core.result.CommonCode;
import com.example.edgecustomer.core.result.Result;
import com.example.edgecustomer.core.result.ResultGenerator;
import com.example.edgecustomer.model.User;
import com.example.edgecustomer.service.DiyService;
import com.example.edgecustomer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin
@RestController
public class DiyController {
    @Autowired
    UserService userService;
    @Autowired
    DiyService diyService;

    /**
     *  如果 diyName 不存在则新建，如果 diyName 存在则更新，如果有 pubConfig.json 则发布到应用市场
     * @param userName
     * @param diyName
     * @param file  docker-compose.yaml,k8s.yaml,pubConfig.json 压缩包文件
     * @return
     */
    @PostMapping("/diy/save")
    public Result saveDiy(String userName, @RequestParam("file") MultipartFile file) throws IOException {
        User user = userService.getUser(userName);
        Integer userId = user.getId();
        try{
            diyService.saveDiy(userId,file);
        }catch (Exception e){
            return ResultGenerator.genFailResult(CommonCode.SERVICE_ERROR,e.toString(),e.getMessage());
        }
        return ResultGenerator.genSuccessResult("success");
    }
}
