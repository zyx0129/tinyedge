from device import Device
import json
import time
from client import Message
import requests
import psutil

class DeviceClient(Device):
    def __init__(self):
        self.__deviceList = {}
        super(DeviceClient, self).__init__()

    def initClient(self):
        return

    def getMemCpu(self):
        data = psutil.virtual_memory()
        total = data.total  # 总内存,单位为byte
        #print('total',total)
        free = data.available  # 可用内存
        #print('free',free)
        memory = "%d" % (int(round(data.percent))) + "%" + " "  # 内存使用情况
        cpu = "%0.2f" % psutil.cpu_percent(interval=1) + "%"  # CPU占用情况
        disk = "%0.2f" % psutil.disk_usage('/').percent + "%" + " "  # 内存使用情况
        print('memory:'+ memory+ ", cpu:"+cpu + ", disk:" + disk)
        return cpu,memory,disk

    def clientRun(self):
        while 1:
            cpu,memory,disk = self.getMemCpu()
            config = self.getConfig()
            #%$@cloud%{
            routeMsg = Message()
            routeMsg.dataType = "system"
            routeMsg.data = {"cpu":cpu,"memory":memory, "disk":disk}
            #msg ={"type":"system","info":{"cpu":cpu,"memory":memory, "disk":disk}}
            self.publish(routeMsg)
            #%}
            time.sleep(5)

        


    
           

    
    
