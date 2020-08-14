package com.example.edgecustomer.service;

import com.example.edgecustomer.model.Device;
import com.example.edgecustomer.model.Mqtt;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public interface DeviceService {

    public List<Device> getDeviceList(Integer systemId);
    public String getDeviceSecret(String deviceKey);
    public void addDevice( Integer systemId, String name, String desp , String userName ,String systemName) throws NoSuchAlgorithmException, IOException;
    public void updateStatus(Mqtt mqtt);
    public void handleDevice(Integer systemId, String deviceName, String userName, String systemName) throws NoSuchAlgorithmException, IOException;
    public Device getDeviceInfo(Integer deviceId);
    public void deleteDevice( Integer deviceId, String deviceName, String systemName, String userName);
}
