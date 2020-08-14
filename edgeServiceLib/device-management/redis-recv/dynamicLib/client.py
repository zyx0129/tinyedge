from kafka import KafkaConsumer
import json
#from msgRec import on_message_come 
import time
from base import Redis
import os


class Message(object):
    def __init__(self,msg = None):
        if msg:
            try:
                msg = json.loads(msg)
                self.dataType = msg["dataType"]
                self.data = msg["data"]
                self.deviceName = msg["deviceName"]
                self.time = msg["time"]
            except Exception as e:
                print("wrong msg:"+str(msg))
        else:
            self.data = None
            self.dataType = None
            self.deviceName = None
            self.time = time.time()

class Client:
    def __init__(self):
        self.__scene = None
        self.__config = None
        self.updateConfig()

    def updateConfig(self):
        configPath = "/app/config/custom.json"
        if os.path.exists(configPath):
            with open(configPath,'r') as load_f:
                configJson = json.load(load_f)
                self.__scene = configJson["scene"]
                self.__config = configJson["serviceConfig"]
    
    def getConfig(self,service ="#serviceName"):
        return self.__config[service]



class AppClient():
    def __init__(self):
        self.__consumer = None
        self.initConsumer()

    def initConsumer(self):
        try:
            self.__consumer = KafkaConsumer(bootstrap_servers=['edge-kafka:9092'],metadata_max_age_ms = 1000)
        except Exception as e:
            print("waiting kafka client start...")
            time.sleep(2)
            self.initConsumer()
    
    def startListen(self):   
        client = Client()
        self.__consumer.subscribe(topics = "cache_redis")
        print("start subscribe")
        redis = Redis()
        for msg in self.__consumer:
            print("msg come")
            value = msg.value
            value = str(value,"utf-8")
            print(value)
            routeMsg = Message(value)
            deviceName = routeMsg.deviceName
            data = routeMsg.data
            for key,value in data.items():
                print(key)
                print(value)
                redis.updateData(deviceName,key,value,routeMsg.time)
            #on_message_come(client,routeMsg)