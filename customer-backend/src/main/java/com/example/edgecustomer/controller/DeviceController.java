package com.example.edgecustomer.controller;

import com.example.edgecustomer.core.result.CommonCode;
import com.example.edgecustomer.core.result.Result;
import com.example.edgecustomer.core.result.ResultGenerator;
import com.example.edgecustomer.model.Device;
import com.example.edgecustomer.model.Mqtt;
import com.example.edgecustomer.model.Topic;
import com.example.edgecustomer.service.DeviceService;
import com.example.edgecustomer.service.TopicService;
import com.example.edgecustomer.utils.FileOperater;
import com.example.edgecustomer.utils.JWTUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@CrossOrigin
@RestController
public class DeviceController {

    @Autowired
    DeviceService deviceService;

    @PostMapping("device/auth")
    public ResponseEntity<String> deviceAuth(String deviceKey, String  deviceSecret) {
        /*String password = deviceService.getDeviceSecret(deviceKey);
        if (password.equals(deviceSecret)){
            return new ResponseEntity<>("", HttpStatus.OK);
        }else{
            return new ResponseEntity<>("", HttpStatus.FORBIDDEN);
        }*/
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @PostMapping("device/superuser")
    public ResponseEntity<String> superuser(String deviceKey) {
        return new ResponseEntity<>("", HttpStatus.FORBIDDEN);
    }

    @PostMapping("device/acl")
    public ResponseEntity<String> acl(String deviceKey) {
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @PostMapping("device/updateStatus")
    public Result updateStatus(@RequestBody Mqtt mqtt) {
        deviceService.updateStatus(mqtt);
        return ResultGenerator.genSuccessResult("success");
    }



    @PostMapping("/device/add")
    @RequiresAuthentication
    public Result addDevice(HttpServletRequest request,String systemName, Integer systemId, String deviceName, String desp) throws IOException, NoSuchAlgorithmException {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        deviceService.addDevice(systemId, deviceName , desp , username ,systemName);
        List<Device> deviceList = deviceService.getDeviceList(systemId);
        return ResultGenerator.genSuccessResult(deviceList);
    }

    @PostMapping("/device/delete")
    @RequiresAuthentication
    public Result deleteDevice(HttpServletRequest request, Integer deviceId, String deviceName, String systemName) throws IOException, NoSuchAlgorithmException {
        String token = request.getHeader("Authorization");
        String username = JWTUtil.getUsername(token);
        deviceService.deleteDevice(deviceId,deviceName,systemName,username);
        return ResultGenerator.genSuccessResult("success");
    }

    @GetMapping("/device/list")
    @RequiresAuthentication
    public Result listDevices(Integer systemId) {
        List<Device> deviceList = deviceService.getDeviceList(systemId);
        return ResultGenerator.genSuccessResult(deviceList);
    }

    @GetMapping("/device/info")
    @RequiresAuthentication
    public Result getDeviceInfo(Integer deviceId) {
        Device device = deviceService.getDeviceInfo(deviceId);
        return ResultGenerator.genSuccessResult(device);
    }

}
