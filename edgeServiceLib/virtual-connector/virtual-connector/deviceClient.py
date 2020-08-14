from device import Device
import json
import time
from client import Message
import requests
from myThread import createThread
import random

class DeviceClient(Device):
    def __init__(self):
        self.__deviceList = {}
        super(DeviceClient, self).__init__()
        self.__ledStatus = 0

    def initClient(self):
        def virtualSensor():
            deviceName = "virtual_smart_sensor"
            appId = "app_1"
            if self.checkServiceIfExist("device-management"):
                self.registerDevice(deviceName)
                self.onlineDevice(deviceName)
            else:
                print("Warning: The device-management module does not exsit,and the device "+deviceName+" cannot be registered")
            while 1:
                temp = round(random.uniform(25,35),2)
                light = random.randint(0,600)
                humi = round(random.uniform(70,80),2)
                routeMsg = Message()
                routeMsg.dataType = "data"
                routeMsg.deviceName = deviceName
                routeMsg.appId = appId
                routeMsg.data = {"temp":temp,"light":light, "humi":humi}
                print(deviceName + " : {temp:"+ str(temp)+ ", light:"+str(light) + ", humi:" + str(humi)+"}")
                self.publish(routeMsg)
                time.sleep(5)
        def virtualLed():
            deviceName = "virtual_smart_led"
            appId = "app_2"
            if self.checkServiceIfExist("device-management"):
                self.registerDevice(deviceName)
                self.onlineDevice(deviceName)
            else:
                print("Warning: The device-management module does not exsit,and the device "+deviceName+" cannot be registered")
            while 1:
                routeMsg = Message()
                routeMsg.dataType = "data"
                routeMsg.deviceName = deviceName
                routeMsg.appId = appId
                routeMsg.data = {"led":self.__ledStatus}
                print(deviceName + " : {led:"+ str(self.__ledStatus)+ "}")
                self.publish(routeMsg)
                time.sleep(5)
        createThread(virtualSensor)
        createThread(virtualLed)

    def callService(self,deviceName,service,payload):
        if deviceName=="virtual_smart_led":
            if service=="turnOnLed":
                self.__ledStatus=1
                print("Led turnOn success")
            elif service=="turnOffLed":
                self.__ledStatus=0
                print("Led turnOff success")
            else:
                print("Undefined service "+service+" for "+deviceName)
        else:
            print("Undefined device "+deviceName)

    def setProperties(self,deviceName,payload):
        if deviceName=="virtual_smart_led":
            if "led" in payload:
                if payload["led"]==1:
                    self.__ledStatus=1
                    print("Led turnOn success")
                elif payload["led"]==0:
                    self.__ledStatus=0
                    print("Led turnOff success")
        else:
            print("Undefined device "+deviceName)




    
           

    
    
