import json
import os 

class JsonObj:
    def __init__(self):
        self.__deviceInfoPath = "/app/config/deviceInfo.json"

    def read(self):
        deviceInfo = {}
        if os.path.exists(self.__deviceInfoPath):
            with open(self.__deviceInfoPath,'r') as load_f:
                deviceInfo = json.load(load_f)
        return deviceInfo
           
    def write(self, deviceInfo):
        with open(self.__deviceInfoPath,"w") as dump_f:
            json.dump(deviceInfo,dump_f)
