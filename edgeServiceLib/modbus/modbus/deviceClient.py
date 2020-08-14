from device import Device
import json
import time
from client import Message
from myThread import createThread
from modbus_tk import modbus_tcp
import modbus_tk.defines as cst
import requests

class DeviceClient(Device):
    def __init__(self):
        self.__deviceList = {}
        super(DeviceClient, self).__init__()

    def initClient(self):

        def monitor():
            while 1:
                for deviceName,device in self.__deviceList.items():
                    status = device["status"]
                    appId = device["appId"]
                    if status == "online":
                        for propertyName, task in device["tasks"].items():
                            if time.monotonic()>task["next"]:
                                master = device["master"]
                                values = None
                                try:
                                    values = master.execute(1, cst.READ_HOLDING_REGISTERS, task["startAddress"], task["count"])
                                    #print("new value:"+str(values))
                                except:
                                    print("device "+ deviceName +" offline")
                                    #offline
                                    device["status"] = "reconnect"
                                    device["next"] = time.monotonic()+device["retryInterval"]
                                    self.offlineDevice(deviceName)
                                    break
                                task["next"] = time.monotonic()+ task["pollingInterval"]
                                for value in values:
                                    if self.getScene() and "cache" in self.getScene():
                                        cacheScene = self.getScene()["cache"]
                                        if cacheScene:
                                            self.updateCache(deviceName,propertyName,value)
                                    routeMsg = Message()
                                    routeMsg.dataType = "data"
                                    routeMsg.deviceName = deviceName
                                    routeMsg.appId = appId
                                    routeMsg.data = {propertyName:value}
                                    self.publish(routeMsg)
                    else:
                        if time.monotonic()>device["next"]:
                            task = None
                            for propertyName in device["tasks"]:
                                task = device["tasks"][propertyName]
                                break
                            master = device["master"]
                            try:
                                master.execute(1, cst.READ_HOLDING_REGISTERS, task["startAddress"], task["count"])
                                device["status"] = "online"
                                self.onlineDevice(deviceName)
                            except:
                                print("device "+ deviceName +" offline")
                                if status == "reconnect":
                                    device["retryCount"] +=1
                                    if device["retryCount"] == device["maxRetry"]:
                                        device["status"] = "close" 
                                        device["retryCount"] = 0
                                        device["next"] = time.monotonic()+device["sessionInterval"]
                                    else:
                                        device["next"] = time.monotonic()+device["retryInterval"]
                                else:  #close
                                    device["next"] = time.monotonic()+device["sessionInterval"]

                time.sleep(0.1) 
                
        config = self.getConfig()
        if not config or "slaveConfigs" in config:
            print("No slaveConfigs found")
        salveConfigs = config["salveConfigs"]
        for deviceName,deviceInfo in salveConfigs.items():
            device = {}
            #device["deviceName"] = deviceName
            if "appId" in deviceInfo:
                device["appId"] = deviceInfo["appId"]
            else:
                device["appId"] = "app_1"
            if "retryInterval" in deviceInfo:
                device["retryInterval"] = deviceInfo["retryInterval"]
            else:
                device["retryInterval"] = 5
            if "maxRetry" in deviceInfo:
                device["maxRetry"] = deviceInfo["maxRetry"]
            else:
                device["maxRetry"] = 5
            if "sessionInterval" in deviceInfo:
                device["sessionInterval"] = deviceInfo["sessionInterval"]
            else:
                device["sessionInterval"] = 20
            device["retryCount"] = 0
            device["status"] = "offline"
            device["next"] = time.monotonic()
            if self.checkServiceIfExist("device-management"):
                self.registerDevice(deviceName)
                address = ""
                if deviceInfo["type"] == "TCP":
                    address = deviceInfo["ip"]+":"+str(deviceInfo["port"])
                self.updateAddress(deviceName,address)
            else:
                print("Warning: The device-management module does not exsit,and the device "+deviceName+" cannot be registered")
            for propertyName,propertyInfo in deviceInfo["properties"].items():
                propertyInfo["next"] = time.monotonic()+ propertyInfo["pollingInterval"]
            device["tasks"] = deviceInfo["properties"]
            # taskList = {}
            # for propertyName,propertyInfo in deviceInfo["properties"].items():
            #     task = propertyInfo
            #     task["next"] = time.monotonic()+ propertyInfo["pollingInterval"]
            #     task["propertyName"] = propertyName
            #     #task["deviceName"] = deviceName
            #     taskList.append(task)
            # device["taskList"] = taskList
            if deviceInfo["type"] == "TCP":
                master = modbus_tcp.TcpMaster(host = deviceInfo["ip"],port = deviceInfo["port"])
                device["master"] = master
            self.__deviceList[deviceName] = device
        #print(self.__deviceList)
        #createThread(monitor,self)
        createThread(monitor)

    

    def setProperties(self,deviceName,payload):
        device = self.__deviceList[deviceName]
        master = device["master"]
        for propertyName,value in payload.items():
            task = device["tasks"][propertyName]
            address = task["startAddress"]
            master.execute(1, cst.WRITE_SINGLE_REGISTER, address, output_value=value)


    
           

    
    
