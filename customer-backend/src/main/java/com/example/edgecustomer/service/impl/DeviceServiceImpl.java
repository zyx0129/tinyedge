package com.example.edgecustomer.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.edgecustomer.mapper.DeviceMapper;
import com.example.edgecustomer.model.Device;
import com.example.edgecustomer.model.Mqtt;
import com.example.edgecustomer.service.DeviceService;
import com.example.edgecustomer.service.TopicService;
import com.example.edgecustomer.utils.FileOperater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

@Service
public class DeviceServiceImpl implements DeviceService {
    @Value("${userPath}")
    private String userPath;
    @Value("${mqttServerIp}")
    private String mqttServerIp;
    @Value("${mqttServerPort}")
    private String mqttServerPort;

    @Autowired
    DeviceMapper deviceMapper;
    @Autowired
    TopicService topicService;

    @Override
    public void addDevice(Integer systemId, String deviceName, String desp, String userName, String systemName) throws NoSuchAlgorithmException, IOException {
        Device device = new Device();
        String deviceKey = sha1(systemId.toString() + "*" + deviceName);
        String deviceSecret = sha1(systemId.toString() + "*" + deviceName + "*" + System.currentTimeMillis());
        device.setName(deviceName);
        device.setSystemId(systemId);
        device.setDeviceKey(deviceKey);
        device.setDeviceSecret(deviceSecret);
        device.setDescription(desp);
        device.setStatus("未部署");
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        device.setCreatedTime(timestamp);
        deviceMapper.insert(device);
        String systemDirPath = userPath + "/" + userName + "/" + systemName;
        String newCloudDirbPath = systemDirPath  + "/system/cloud";
        if (new File(newCloudDirbPath).exists()) {   //处理云文件
            Integer deviceId = device.getId();
            String dataTopic = deviceKey + "/data/post";
            String serviceTopic = deviceKey + "/service/set";
            topicService.addTopic(deviceId, dataTopic);
            topicService.addTopic(deviceId, serviceTopic);
            String configPath = systemDirPath + "/custom.json";
            String configStr = FileOperater.readFile(configPath);
            JSONObject customJson=JSONObject.parseObject(configStr);
            JSONObject moduleInfoJson = customJson.getJSONObject("serviceConfig");
            if(moduleInfoJson.containsKey("cloud")){
                JSONObject cloudJson = moduleInfoJson.getJSONObject("cloud");
                if(!cloudJson.containsKey("serverIp")) {  //如果用户没有配置，默认是服务器的ip
                    cloudJson.put("serverIp", mqttServerIp);
                    cloudJson.put("serverPort", Integer.parseInt(mqttServerPort));
                    cloudJson.put("deviceKey", deviceKey);
                    cloudJson.put("deviceSecret", deviceSecret);
                    cloudJson.put("dataTopic", dataTopic);
                    cloudJson.put("serviceTopic", serviceTopic);
                }
            }
            String deviceConfigPath = systemDirPath+"/"+deviceName+"_custom.json";
            FileOperater.writeFile(customJson.toJSONString(),deviceConfigPath);
        }
        /*String systemFilePath = userPath + "/" + userName + "/" + systemName + "/system";
        String deviceFilePath = userPath + "/" + userName + "/" + systemName + "/device/" + deviceName;
        FileOperater.copyDir(systemFilePath, deviceFilePath + "/system");
        String newCloudDirbPath = deviceFilePath + "/system/cloud";
        if (new File(newCloudDirbPath).exists()) {   //处理云文件
            String cloudFilePath = newCloudDirbPath + "/cloud/dynamicLib/msgRec.py";
            File cloudFile = new File(cloudFilePath);
            String str = FileOperater.readFile(cloudFile.getPath());
            String finalStr = str.replace("#deviceKey", deviceKey);
            finalStr = finalStr.replace("#deviceSecret", deviceSecret);
            finalStr = finalStr.replace("#serverIp", mqttServerIp);
            finalStr = finalStr.replace("#serverPort", mqttServerPort);
            finalStr = finalStr.replace("#topic", topicName);
            FileOperater.writeFile(finalStr, cloudFilePath);
        }*/

        /*String zipPath = deviceFilePath + "/system.zip";
        //String sourceFile = "/edge-system/user/zyx/dfdf/output";
        //这个是压缩之后的文件绝对路径
        FileOutputStream fos = new FileOutputStream(zipPath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(deviceFilePath + "/system");

        FileOperater.zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();*/
    }

    @Override
    public void handleDevice(Integer systemId, String deviceName, String userName, String systemName) throws NoSuchAlgorithmException, IOException {
        Example example = new Example(Device.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("systemId", systemId);
        criteria.andEqualTo("name", deviceName);
        Device device = deviceMapper.selectOneByExample(example);
        if (device == null) {
            addDevice(systemId,deviceName,"linklab device",userName,systemName);
        }
    }


    @Override
    public void deleteDevice(Integer deviceId, String deviceName, String systemName, String userName){
        deviceMapper.deleteByPrimaryKey(deviceId);
        String deviceFilePath = userPath + "/" + userName + "/" + systemName + "/"+deviceName+"_custom.json";
        FileOperater.deleteDir(deviceFilePath);
    }

    @Override
    public List<Device> getDeviceList(Integer systemId){
        Example example=new Example(Device.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("systemId",systemId);
        List<Device> deviceList = deviceMapper.selectByExample(example);
        return deviceList;
    }

    @Override
    public String getDeviceSecret(String deviceKey){
        Example example=new Example(Device.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("deviceKey",deviceKey);
        String deviceSecret = deviceMapper.selectOneByExample(example).getDeviceSecret();
        return deviceSecret;
    }

    @Override
    public void updateStatus(Mqtt mqtt){
        String action = mqtt.getAction();
        Example example=new Example(Device.class);
        Example.Criteria criteria = example.createCriteria();
        String deviceKey = mqtt.getUsername();
        criteria.andEqualTo("deviceKey",deviceKey);
        Device device = new Device();
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        device.setChangedTime(timestamp);
        if (action.equals("client_connected")){
            String status = checkStatus(deviceKey);
            if(status.equals("未部署")){  //第一次上线
                device.setActivatedTime(timestamp);
            }
            device.setStatus("在线");
            device.setIp(mqtt.getIpaddress());
        }else{
            device.setStatus("离线");
        }
        deviceMapper.updateByExampleSelective(device,example);
    }

    @Override
    public Device getDeviceInfo(Integer deviceId){
        Example example=new Example(Device.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",deviceId);
        Device device = deviceMapper.selectOneByExample(example);
        return device;
    }

    public String checkStatus(String deviceKey){
        Example example=new Example(Device.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("deviceKey",deviceKey);
        String status = deviceMapper.selectOneByExample(example).getStatus();
        return  status;
    }




    public static String sha1(String data) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(data.getBytes());
        StringBuffer buf = new StringBuffer();
        byte[] bits = md.digest();
        for(int i=0;i<bits.length;i++){
            int a = bits[i];
            if(a<0) a+=256;
            if(a<16) buf.append("0");
            buf.append(Integer.toHexString(a));
        }
        return buf.toString();
    }

}
